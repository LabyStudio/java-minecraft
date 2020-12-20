package de.labystudio.game.world.block;

import de.labystudio.game.util.EnumBlockFace;

public class BlockLog extends Block {

    public BlockLog(int id, int textureSlot) {
        super(id, textureSlot);
    }

    @Override
    protected int getTextureForFace(EnumBlockFace face) {
        return this.textureSlotId + (face.isYAxis() ? 1 : 0);
    }
}
