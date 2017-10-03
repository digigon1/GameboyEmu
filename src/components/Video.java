package components;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Goncalo on 02/10/2017.
 */
public class Video {
    Frame f;
    Graphics2D graphics2D;
    char scrollX; //0xFF42
    char scrollY; //0xFF43

    char[] video; //0x8000 - 0x9FFF
    char[] oam; //0xFE00 - 0xFE9F
    char lcdc; //0xFF40
    char stat; //0xFF41
    char bgp; //0xFF47
    char obp0, obp1; //0xFF48, 0xFF49

    public Video(String name) {
        video = new char[8096];
        oam = new char[160];
        lcdc = 0;
        stat = 0;

        f = new JFrame(name);
        f.setSize(160, 144);
        f.setVisible(true);

        new Thread(() -> {
            if (displayEnable()) {
                updateGraphics();
            } else {
                disableDisplay();
            }
            f.update(graphics2D);
            try {
                wait((long) (1000.0/60));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void disableDisplay() {
        graphics2D.setColor(Color.WHITE);
        graphics2D.drawRect(0, 0, 160, 144);
    }

    private boolean getBit(char c, int i) {
        return (c & (1 << i)) > 0;
    }

    private boolean displayEnable() {
        return getBit(lcdc, 7);
    }

    private char baseWindowTileMap() {
        return (char) (getBit(lcdc, 6)?0x9C00:0x9800);
    }

    private char baseTileData() {
        return (char) (getBit(lcdc, 4)?0x8000:0x8800);
    }

    private char baseBackgroundTileMap() {
        return (char) (getBit(lcdc, 3)?0x9C00:0x9800);
    }

    private boolean largeSpriteSize() {
        return getBit(lcdc, 2);
    }

    private int getBackgroundShade(int i) {
        return (getBit(bgp, i * 2)?2:0) + (getBit(bgp, i * 2 + 1)?1:0);
    }

    private int getObject0Shade(int i) {
        return (getBit(obp0, i * 2)?2:0) + (getBit(obp0, i * 2 + 1)?1:0);
    }

    private int getObject1Shade(int i) {
        return (getBit(obp1, i * 2)?2:0) + (getBit(obp1, i * 2 + 1)?1:0);
    }

    private void updateGraphics() {

    }
}
