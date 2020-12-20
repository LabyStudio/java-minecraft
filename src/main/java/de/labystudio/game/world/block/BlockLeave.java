package de.labystudio.game.world.block;

public class BlockLeave extends Block {

    public BlockLeave(int id, int textureSlot) {
        super(id, textureSlot);
    }

    @Override
    public float getOpacity() {
        return 0.9F;
    }
}
