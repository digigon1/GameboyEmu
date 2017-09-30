import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Goncalo on 30/09/2017.
 */
public class Cartridge {
    RandomAccessFile cart;

    public Cartridge(String cartFileName) throws FileNotFoundException {
         cart = new RandomAccessFile(cartFileName, "r");
    }

    public byte read(char address) throws IOException {
        cart.seek(address);
        return cart.readByte();
    }

    public byte readRam(int address) {
        return -1; //TODO
    }

    public void writeRam(int i, byte value) {
        //TODO
    }
}
