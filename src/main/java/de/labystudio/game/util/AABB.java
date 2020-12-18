package de.labystudio.game.util;

public class AABB {
    private double epsilon = 0.0F;
    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;

    public AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public AABB clone() {
        return new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public AABB expand(double xa, double ya, double za) {
        double _x0 = this.minX;
        double _y0 = this.minY;
        double _z0 = this.minZ;
        double _x1 = this.maxX;
        double _y1 = this.maxY;
        double _z1 = this.maxZ;
        if (xa < 0.0F) {
            _x0 += xa;
        }
        if (xa > 0.0F) {
            _x1 += xa;
        }
        if (ya < 0.0F) {
            _y0 += ya;
        }
        if (ya > 0.0F) {
            _y1 += ya;
        }
        if (za < 0.0F) {
            _z0 += za;
        }
        if (za > 0.0F) {
            _z1 += za;
        }
        return new AABB(_x0, _y0, _z0, _x1, _y1, _z1);
    }

    public AABB grow(double xa, double ya, double za) {
        double _x0 = this.minX - xa;
        double _y0 = this.minY - ya;
        double _z0 = this.minZ - za;
        double _x1 = this.maxX + xa;
        double _y1 = this.maxY + ya;
        double _z1 = this.maxZ + za;

        return new AABB(_x0, _y0, _z0, _x1, _y1, _z1);
    }

    public double clipXCollide(AABB c, double xa) {
        if ((c.maxY <= this.minY) || (c.minY >= this.maxY)) {
            return xa;
        }
        if ((c.maxZ <= this.minZ) || (c.minZ >= this.maxZ)) {
            return xa;
        }
        if ((xa > 0.0F) && (c.maxX <= this.minX)) {
            double max = this.minX - c.maxX - this.epsilon;
            if (max < xa) {
                xa = max;
            }
        }
        if ((xa < 0.0F) && (c.minX >= this.maxX)) {
            double max = this.maxX - c.minX + this.epsilon;
            if (max > xa) {
                xa = max;
            }
        }
        return xa;
    }

    public double clipYCollide(AABB c, double ya) {
        if ((c.maxX <= this.minX) || (c.minX >= this.maxX)) {
            return ya;
        }
        if ((c.maxZ <= this.minZ) || (c.minZ >= this.maxZ)) {
            return ya;
        }
        if ((ya > 0.0F) && (c.maxY <= this.minY)) {
            double max = this.minY - c.maxY - this.epsilon;
            if (max < ya) {
                ya = max;
            }
        }
        if ((ya < 0.0F) && (c.minY >= this.maxY)) {
            double max = this.maxY - c.minY + this.epsilon;
            if (max > ya) {
                ya = max;
            }
        }
        return ya;
    }

    public double clipZCollide(AABB c, double za) {
        if ((c.maxX <= this.minX) || (c.minX >= this.maxX)) {
            return za;
        }
        if ((c.maxY <= this.minY) || (c.minY >= this.maxY)) {
            return za;
        }
        if ((za > 0.0F) && (c.maxZ <= this.minZ)) {
            double max = this.minZ - c.maxZ - this.epsilon;
            if (max < za) {
                za = max;
            }
        }
        if ((za < 0.0F) && (c.minZ >= this.maxZ)) {
            double max = this.maxZ - c.minZ + this.epsilon;
            if (max > za) {
                za = max;
            }
        }
        return za;
    }

    public boolean intersects(AABB c) {
        if ((c.maxX <= this.minX) || (c.minX >= this.maxX)) {
            return false;
        }
        if ((c.maxY <= this.minY) || (c.minY >= this.maxY)) {
            return false;
        }
        if ((c.maxZ <= this.minZ) || (c.minZ >= this.maxZ)) {
            return false;
        }
        return true;
    }

    public void move(double xa, double ya, double za) {
        this.minX += xa;
        this.minY += ya;
        this.minZ += za;
        this.maxX += xa;
        this.maxY += ya;
        this.maxZ += za;
    }

    public AABB offset(double x, double y, double z) {
        return new AABB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }
}
