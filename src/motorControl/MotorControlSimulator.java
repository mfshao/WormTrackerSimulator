package motorControl;

import static dto.Properties.IMAGE_HEIGHT;
import static dto.Properties.IMAGE_WIDTH;
import static dto.Properties.MOTOR_MOVE_DELAY;
import static dto.Properties.MOTOR_PX_PER_STEP_X;
import static dto.Properties.MOTOR_PX_PER_STEP_Y;
import static dto.Properties.MOTOR_STEP_MULTIPLIER;
import static dto.Properties.MOTOR_TICK_RATE;
import static dto.Properties.MOVE_DECISION_BOUNDARY_PX;
import static dto.Properties.MOVE_DECISION_POST_DELAY;
import static dto.Properties.SEGMENTATION_DELAY;
import static dto.Properties.SEGMENTATION_FAILURE_THRESHOLD;
import imageProcessing.ImageProcessor;

/**
 * Motor Controller. Independently controls the motors to keep the target in
 * frame. Controls only 1 set of motors.
 *
 * @author Kyle Moy
 *
 */
public class MotorControlSimulator implements Runnable {

    private static volatile int x = 0; //Not int[2] because reference passing
    private static volatile int y = 0;
    private static volatile int moving = 0;
    private static volatile int[] centroid = {0, 0};

    //private PrintWriter os;
    private volatile boolean run = true;
    private final Thread thread;
    private ImageProcessor imageProcessor;
    private long lastMove;

    public MotorControlSimulator() {
        thread = new Thread(this);
    }

    public void attach(ImageProcessor imageProcessor) {
        this.imageProcessor = imageProcessor;
    }

    public void detach() {
        this.imageProcessor = null;
    }
    

    /**
     * Publically accessible move command, for use in UI (click drag)
     *
     * @param x pixels to move horizontally
     * @param y pixels to move vertically
     */
    public void move(int x, int y) {
        int xStepOffset = (int) (x / MOTOR_PX_PER_STEP_X * MOTOR_STEP_MULTIPLIER) * -1;
        int yStepOffset = (int) (y / MOTOR_PX_PER_STEP_Y * MOTOR_STEP_MULTIPLIER);
        enable();
        //Request motor move
        move(MOTOR_MOVE_DELAY, xStepOffset, yStepOffset);

        disable();
    }

    /**
     * Wrapper command for EBB command
     *
     * @param speed Time in milliseconds during which to execute the move
     * (longer == slower)
     * @param xStep Number of steps for motor 1 to move
     * @param yStep Number of steps for motor 2 to move
     */
    private void move(int speed, int xStep, int yStep) {
        try {
            Thread.sleep((int) (MOTOR_MOVE_DELAY * 1.1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Update our location
        //Is += an atomic operation? Who knows.
        x += xStep;
        y += yStep;
    }

    /**
     * @return If we're currently in a move operation
     */
    public static int isMoving() {
        return moving;
    }


    /**
     * Initializes the motor step mode, and flag us as moving until disable()
     */
    private void enable() {
        moving = 1;
        //os.println("ON\t" + System.currentTimeMillis());
//        write(String.format("EM,%d,%d", MOTOR_STEP_MODE, MOTOR_STEP_MODE));
    }

    /**
     * Cuts power to the motors. This unlocks the motors. Might prevent
     * vibrations, might keep the motors cool.
     */
    private void disable() {
        moving = 0;
        //os.println("OFF\t" + System.currentTimeMillis());
        //os.flush();
//        write(String.format("EM,%d,%d", 0, 0));
    }

    /**
     * Repositions the camera such that a given point in the camera view is in
     * the center. This can go horribly wrong if the MOTOR_PX_PER_STEP
     * calibration is off even by a little bit.
     *
     * @param targetLocation The location to center
     */
    private void center(double[] targetLocation) {
        //How much do we have to move, in pixels, to center the centroid?
        int xPxOffset = (IMAGE_WIDTH / 2) - (int) targetLocation[0];
        int yPxOffset = (IMAGE_HEIGHT / 2) - (int) targetLocation[1];

        // Full step mode, integer values only
        int xStepOffset = (int) (xPxOffset / MOTOR_PX_PER_STEP_X * MOTOR_STEP_MULTIPLIER) * -1;
        int yStepOffset = (int) (yPxOffset / MOTOR_PX_PER_STEP_Y * MOTOR_STEP_MULTIPLIER);

        //Enabled/Disable motors only when needed
        //Theoretically prevents oscillation between partial steps, but understanding the actual mechanics of stepper motors, that's not actually possible
        //But left in anyway because it allows the user to manually move the stage, and prevents lock up in the case of program failure
        enable();

        //Request motor move
        move(MOTOR_MOVE_DELAY, xStepOffset, yStepOffset);

        disable();
    }

    /**
     * @return The current x offset in pixels of the camera since initialization
     */
    public static int x() {
        return x;
    }

    /**
     * @return The current y offset in pixels of the camera since initialization
     */
    public static int y() {
        return y;
    }

    public static int[] getCentroid() {
        return centroid;
    }

    /**
     * Run this object in another thread
     */
    public void start() {
        //Holy cow this is atrocious design.
        //I am so sorry.
        //Someone take my future CS degree from me.
        run = true;
        if (!thread.isAlive()) {
            thread.start();
        }
    }

    /**
     * Stops the thread
     */
    public void stop() {
        run = false;
    }

    @Override
    public void run() {
        if (imageProcessor == null) {
            throw new Error("ImageProcessor not attached.");
        }
        lastMove = System.currentTimeMillis();
        while (true) {
            if (!run) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                continue;
            }
            double[] target = imageProcessor.getCentroid();
            if (target == null) {
                try {
                    Thread.sleep(SEGMENTATION_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            centroid[0] = (int) Math.round(target[0]);
            centroid[1] = (int) Math.round(target[1]);

            if (target[0] <= MOVE_DECISION_BOUNDARY_PX
                    || target[0] >= IMAGE_WIDTH - MOVE_DECISION_BOUNDARY_PX
                    || target[1] <= MOVE_DECISION_BOUNDARY_PX
                    || target[1] >= IMAGE_HEIGHT - MOVE_DECISION_BOUNDARY_PX) {
                if (imageProcessor.isConfident()) {
                    center(target);
                    imageProcessor.getNewReference();
                    lastMove = System.currentTimeMillis();
                    try {
                        Thread.sleep(MOVE_DECISION_POST_DELAY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!imageProcessor.run) {
                break;
            }
            if (System.currentTimeMillis() - lastMove > SEGMENTATION_FAILURE_THRESHOLD) {
                imageProcessor.run = false;
                break;
            }
            try {
                Thread.sleep(MOTOR_TICK_RATE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
