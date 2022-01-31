package de.labystudio.game.player;

import de.labystudio.game.util.BoundingBox;
import de.labystudio.game.world.World;
import de.labystudio.game.world.block.Block;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class Player {

    private final World world;

    public double prevX;
    public double prevY;
    public double prevZ;

    public double x;
    public double y;
    public double z;

    public double motionX;
    public double motionY;
    public double motionZ;

    public float yaw;
    public float pitch;

    public BoundingBox boundingBox;
    public boolean onGround = false;
    public boolean collision = false;

    public float jumpMovementFactor = 0.02F;
    protected float speedInAir = 0.02F;
    private final float flySpeed = 0.05F;
    private final float stepHeight = 0.5F;

    float moveForward;
    float moveStrafing;

    private int jumpTicks;
    private int flyToggleTimer;
    private int sprintToggleTimer;

    public boolean jumping;
    public boolean sprinting;
    public boolean sneaking;
    public boolean flying;

    public float prevFovModifier = 0;
    public float fovModifier = 0;
    public long timeFovChanged = 0;

    public Player(World world) {
        this.world = world;
        this.resetPos();
    }

    private void resetPos() {
        this.setPos(0, 76, 0);
    }

    private void setPos(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        float w = 0.3F;
        float h = 0.9F;
        this.boundingBox = new BoundingBox(x - w, y - h, z - w, x + w, y + h, z + w);
    }

    public void turn(float xo, float yo) {
        this.yaw = ((float) (this.yaw + xo * 0.15D));
        this.pitch = ((float) (this.pitch - yo * 0.15D));
        if (this.pitch < -90.0F) {
            this.pitch = -90.0F;
        }
        if (this.pitch > 90.0F) {
            this.pitch = 90.0F;
        }
    }

    public void onTick() {
        float prevMoveForward = this.moveForward;
        boolean prevJumping = this.jumping;

        this.updateKeyboardInput();

        // Toggle jumping
        if (!prevJumping && this.jumping) {
            if (this.flyToggleTimer == 0) {
                this.flyToggleTimer = 7;
            } else {
                this.flying = !this.flying;
                this.flyToggleTimer = 0;

                this.updateFOVModifier();
            }
        }

        // Toggle sprint
        if (prevMoveForward == 0 && this.moveForward > 0) {
            if (this.sprintToggleTimer == 0) {
                this.sprintToggleTimer = 7;
            } else {
                this.sprinting = true;
                this.sprintToggleTimer = 0;

                this.updateFOVModifier();
            }
        }

        if (this.jumpTicks > 0) {
            --this.jumpTicks;
        }

        if (this.flyToggleTimer > 0) {
            --this.flyToggleTimer;
        }

        if (this.sprintToggleTimer > 0) {
            --this.sprintToggleTimer;
        }

        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;

        // Stop if too slow
        if (Math.abs(this.motionX) < 0.003D) {
            this.motionX = 0.0D;
        }
        if (Math.abs(this.motionY) < 0.003D) {
            this.motionY = 0.0D;
        }
        if (Math.abs(this.motionZ) < 0.003D) {
            this.motionZ = 0.0D;
        }

        // Jump
        if (this.jumping) {
            if (this.isInWater()) {
                this.motionY += 0.03999999910593033D;
            } else if (this.onGround && this.jumpTicks == 0) {
                this.jump();
                this.jumpTicks = 10;
            }
        } else {
            this.jumpTicks = 0;
        }

        this.moveStrafing *= 0.98F;
        this.moveForward *= 0.98F;

        if (this.flying) {
            this.travelFlying(this.moveForward, 0, this.moveStrafing);
        } else {
            if (this.isInWater()) {
                // Is inside of water
                this.travelInWater(this.moveForward, 0, this.moveStrafing);
            } else {
                // Is on land
                this.travel(this.moveForward, 0, this.moveStrafing);
            }
        }

        this.jumpMovementFactor = this.speedInAir;

        if (this.sprinting) {
            this.jumpMovementFactor = (float) ((double) this.jumpMovementFactor + (double) this.speedInAir * 0.3D);

            if (this.moveForward <= 0 || this.collision || this.sneaking) {
                this.sprinting = false;

                this.updateFOVModifier();
            }
        }
    }

    public boolean isInWater() {
        return this.world.getBlockAt(this.getBlockPosX(), this.getBlockPosY(), this.getBlockPosZ()) == Block.WATER.getId();
    }

    public boolean isHeadInWater() {
        return this.world.getBlockAt(this.getBlockPosX(), (int) (this.y + this.getEyeHeight() + 0.12), this.getBlockPosZ()) == Block.WATER.getId();
    }

    protected void jump() {
        this.motionY = 0.42D;

        if (this.sprinting) {
            float radiansYaw = (float) Math.toRadians(this.yaw);
            this.motionX -= Math.sin(radiansYaw) * 0.2F;
            this.motionZ += Math.cos(radiansYaw) * 0.2F;
        }
    }

    private void travelFlying(float forward, float vertical, float strafe) {
        // Fly move up and down
        if (this.sneaking) {
            this.moveStrafing = strafe / 0.3F;
            this.moveForward = forward / 0.3F;
            this.motionY -= this.flySpeed * 3.0F;
        }

        if (this.jumping) {
            this.motionY += this.flySpeed * 3.0F;
        }

        double prevMotionY = this.motionY;
        float prevJumpMovementFactor = this.jumpMovementFactor;
        this.jumpMovementFactor = this.flySpeed * (this.sprinting ? 2 : 1);

        this.travel(forward, vertical, strafe);

        this.motionY = prevMotionY * 0.6D;
        this.jumpMovementFactor = prevJumpMovementFactor;

        if (this.onGround) {
            this.flying = false;
        }
    }

    private void travelInWater(float forward, float vertical, float strafe) {
        float slipperiness = 0.8F;
        float friction = 0.02F;

        this.moveRelative(forward, vertical, strafe, friction);
        this.collision = this.moveCollide(-this.motionX, this.motionY, -this.motionZ);

        this.motionX *= slipperiness;
        this.motionY *= 0.800000011920929D;
        this.motionZ *= slipperiness;
        this.motionY -= 0.02D;
    }

    public void travel(float forward, float vertical, float strafe) {
        float prevSlipperiness = this.getBlockSlipperiness() * 0.91F;

        float value = 0.16277136F / (prevSlipperiness * prevSlipperiness * prevSlipperiness);
        float friction;

        if (this.onGround) {
            friction = this.getAIMoveSpeed() * value;
        } else {
            friction = this.jumpMovementFactor;
        }

        this.moveRelative(forward, vertical, strafe, friction);

        // Get new speed
        float slipperiness = this.getBlockSlipperiness() * 0.91F;

        // Move
        this.collision = this.moveCollide(-this.motionX, this.motionY, -this.motionZ);

        // Gravity
        if (!this.flying) {
            this.motionY -= 0.08D;
        }

        // Decrease motion
        this.motionX *= slipperiness;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= slipperiness;
    }

    private float getBlockSlipperiness() {
        return this.onGround ? 0.6F : 1.0F;
    }

    private float getAIMoveSpeed() {
        return this.sprinting ? 0.13000001F : 0.10000000149011612F;
    }

    public void moveRelative(double forward, double up, double strafe, double friction) {
        double distance = strafe * strafe + up * up + forward * forward;

        if (distance >= 1.0E-4F) {
            distance = Math.sqrt(distance);

            if (distance < 1.0F) {
                distance = 1.0F;
            }

            distance = friction / distance;
            strafe = strafe * distance;
            up = up * distance;
            forward = forward * distance;

            double yawRadians = Math.toRadians(this.yaw);
            double sin = Math.sin(yawRadians);
            double cos = Math.cos(yawRadians);

            this.motionX += strafe * cos - forward * sin;
            this.motionY += up;
            this.motionZ += forward * cos + strafe * sin;
        }
    }

    public void updateKeyboardInput() {
        float moveForward = 0.0F;
        float moveStrafe = 0.0F;

        boolean jumping = false;
        boolean sneaking = false;

        if (Keyboard.isKeyDown(19)) { // R
            this.resetPos();
        }
        if ((Keyboard.isKeyDown(200)) || (Keyboard.isKeyDown(17))) { // W
            moveForward++;
        }
        if ((Keyboard.isKeyDown(208)) || (Keyboard.isKeyDown(31))) { // S
            moveForward--;
        }
        if ((Keyboard.isKeyDown(203)) || (Keyboard.isKeyDown(30))) { // A
            moveStrafe++;
        }
        if ((Keyboard.isKeyDown(205)) || (Keyboard.isKeyDown(32))) { // D
            moveStrafe--;
        }
        if ((Keyboard.isKeyDown(57)) || (Keyboard.isKeyDown(219))) { // Space
            jumping = true;
        }
        if (Keyboard.isKeyDown(42)) { // Shift
            if (this.moveForward > 0 && !this.sneaking && !this.sprinting && this.motionX != 0 && this.motionZ != 0) {
                this.sprinting = true;

                this.updateFOVModifier();
            }
        }
        if (Keyboard.isKeyDown(16)) { // Q
            sneaking = true;
        }

        if (sneaking) {
            moveStrafe = (float) ((double) moveStrafe * 0.3D);
            moveForward = (float) ((double) moveForward * 0.3D);
        }

        this.moveForward = moveForward;
        this.moveStrafing = moveStrafe;

        this.jumping = jumping;
        this.sneaking = sneaking;
    }

    public boolean moveCollide(double targetX, double targetY, double targetZ) {

        // Target position
        double originalTargetX = targetX;
        double originalTargetY = targetY;
        double originalTargetZ = targetZ;

        if (this.onGround && this.sneaking) {
            for (; targetX != 0.0D && this.world.getCollisionBoxes(this.boundingBox.offset(targetX, -this.stepHeight, 0.0D)).isEmpty(); originalTargetX = targetX) {
                if (targetX < 0.05D && targetX >= -0.05D) {
                    targetX = 0.0D;
                } else if (targetX > 0.0D) {
                    targetX -= 0.05D;
                } else {
                    targetX += 0.05D;
                }
            }

            for (; targetZ != 0.0D && this.world.getCollisionBoxes(this.boundingBox.offset(0.0D, -this.stepHeight, targetZ)).isEmpty(); originalTargetZ = targetZ) {
                if (targetZ < 0.05D && targetZ >= -0.05D) {
                    targetZ = 0.0D;
                } else if (targetZ > 0.0D) {
                    targetZ -= 0.05D;
                } else {
                    targetZ += 0.05D;
                }
            }

            for (; targetX != 0.0D && targetZ != 0.0D && this.world.getCollisionBoxes(this.boundingBox.offset(targetX, -this.stepHeight, targetZ)).isEmpty(); originalTargetZ = targetZ) {
                if (targetX < 0.05D && targetX >= -0.05D) {
                    targetX = 0.0D;
                } else if (targetX > 0.0D) {
                    targetX -= 0.05D;
                } else {
                    targetX += 0.05D;
                }

                originalTargetX = targetX;

                if (targetZ < 0.05D && targetZ >= -0.05D) {
                    targetZ = 0.0D;
                } else if (targetZ > 0.0D) {
                    targetZ -= 0.05D;
                } else {
                    targetZ += 0.05D;
                }
            }
        }

        // Get level tiles as bounding boxes
        List<BoundingBox> boundingBoxList = this.world.getCollisionBoxes(this.boundingBox.expand(targetX, targetY, targetZ));

        // Move bounding box
        for (BoundingBox aABB : boundingBoxList) {
            targetY = aABB.clipYCollide(this.boundingBox, targetY);
        }
        this.boundingBox.move(0.0F, targetY, 0.0F);

        for (BoundingBox aABB : boundingBoxList) {
            targetX = aABB.clipXCollide(this.boundingBox, targetX);
        }
        this.boundingBox.move(targetX, 0.0F, 0.0F);

        for (BoundingBox aABB : boundingBoxList) {
            targetZ = aABB.clipZCollide(this.boundingBox, targetZ);
        }
        this.boundingBox.move(0.0F, 0.0F, targetZ);

        this.onGround = originalTargetY != targetY && originalTargetY < 0.0F;

        // Stop motion on collision
        if (originalTargetX != targetX) {
            this.motionX = 0.0F;
        }
        if (originalTargetY != targetY) {
            this.motionY = 0.0F;
        }
        if (originalTargetZ != targetZ) {
            this.motionZ = 0.0F;
        }

        // Update position
        this.x = ((this.boundingBox.minX + this.boundingBox.maxX) / 2.0F);
        this.y = this.boundingBox.minY;
        this.z = ((this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0F);

        // Horizontal collision?
        return originalTargetX != targetX || originalTargetZ != targetZ;
    }

    public float getEyeHeight() {
        return this.sneaking ? 1.50F : 1.62F;
    }

    public void updateFOVModifier() {
        float value = 1.0F;

        if (this.sprinting) {
            value += 1;
        }

        if (this.flying) {
            value *= 1.1F;
        }

        this.setFOVModifier((value - 1.0F) * 10F);
    }

    public void setFOVModifier(float fov) {
        this.prevFovModifier = this.fovModifier;
        this.fovModifier = fov;
        this.timeFovChanged = System.currentTimeMillis();
    }

    public float getFOVModifier() {
        long timePassed = System.currentTimeMillis() - this.timeFovChanged;
        float distance = this.prevFovModifier - this.fovModifier;
        long duration = 100;
        float progress = distance / (float) duration * timePassed;
        return timePassed > duration ? this.fovModifier : this.prevFovModifier - progress;
    }

    public int getBlockPosX() {
        return (int) this.x - (this.x < 0 ? 1 : 0);
    }

    public int getBlockPosY() {
        return (int) this.y - (this.y < 0 ? 1 : 0);
    }

    public int getBlockPosZ() {
        return (int) this.z - (this.z < 0 ? 1 : 0);
    }
}
