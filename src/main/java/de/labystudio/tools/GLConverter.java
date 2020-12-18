package de.labystudio.tools;


import org.lwjgl.opengl.GL11;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.lang.reflect.Field;

public class GLConverter {

    public static void main(String[] args) throws Exception {
        String data = (String) Toolkit.getDefaultToolkit()
                .getSystemClipboard().getData(DataFlavor.stringFlavor);

        String out = "";

        for (String line : data.split("\n")) {

            if (line.contains("GL11.") || line.contains("GLU.") || line.contains("ARBVertexBufferObject.")) {
                Field[] fields = GL11.class.getFields();
                for (Field field : fields) {
                    Object value = field.get(null);

                    if (value instanceof Integer) {
                        int i = (int) value;


                        line = line.replace("(" + i + ");", "(GL11." + field.getName() + ");");
                        line = line.replace("(" + i + ",", "(GL11." + field.getName() + ",");
                        line = line.replace("(" + i + ",", "(GL11." + field.getName() +",");
                        line = line.replace(" " + i + ")", " GL11." + field.getName() + ")");
                        line = line.replace(" " + i + ", ", " GL11." + field.getName() + ", ");
                    }
                }
            }

            out += line + "\n";
        }

        StringSelection selection = new StringSelection(out);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }


}
