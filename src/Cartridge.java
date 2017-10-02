import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Goncalo on 30/09/2017.
 */
public class Cartridge {
    RandomAccessFile cart;
    int bank = 1;

    public Cartridge(String cartFileName) throws FileNotFoundException {
         cart = new RandomAccessFile(cartFileName, "r");
    }

    public char read(char address) throws IOException {
        if (address < 0x4000)
            cart.seek(address);
        else
            cart.seek(address - 0x4000 + (bank * 0x4000));

        byte b = cart.readByte();

        return (char) ((b + 256) % 256);
    }

    public char readRam(int address) {
        return 1; //TODO
    }

    public void writeRam(int i, char value) {
        //TODO
    }

    public void changeBank(char value) {
        bank = value;
    }
}
