import java.io.IOException;

/**
 * Created by Goncalo on 30/09/2017.
 */
public class Memory {
    byte[] work, video;
    Cartridge cart;

    public Memory(Cartridge cart) {
        work = new byte[8096];
        video = new byte[8096];
        this.cart = cart;
    }

    public byte read(char address) throws InvalidMemoryReadLocationException, IOException {
        if (address <= 0x00FF) {
            return -1; //TODO
        } else if (address <= 0x014F){
            return -1; //TODO
        } else if(address <= 0x3FFF) {
            return cart.read(address);
        } else if(address <= 0x7FFF) {
            return cart.read(address);
        } else if(address <= 0x97FF) {
            return -1; //TODO
        } else if (address <= 0x9BFF) {
            return -1; //TODO
        } else if (address <= 0x9FFF) {
            return -1; //TODO
        } else if (address <= 0xBFFF) {
            return cart.readRam(address - 0xA000); //TODO
        } else if (address <= 0xDFFF) {
            return work[address - 0xC000]; //TODO check
        } else if (address <= 0xFDFF) {
            return -1; //TODO
        } else if (address <= 0xFE9F) {
            return -1; //TODO
        } else if (address <= 0xFEFF) {

            return -1; //TODO probably fail is needed

        } else if (address <= 0xFF7F) {
            return -1; //TODO
        } else if (address <= 0xFFFE) {
            return -1; //TODO
        } else if (address == 0xFFFF){
            return -1; //TODO
        }

        throw new InvalidMemoryReadLocationException();
    }

    public void write(char address, byte value) throws InvalidMemoryWriteLocationException {
        if (address <= 0x00FF) {
            //TODO
        } else if (address <= 0x014F){
            //TODO
        } else if(address <= 0x3FFF) {
            //TODO
            //return cart.read(address);
        } else if(address <= 0x7FFF) {
            //TODO
            //return cart.read(address);
        } else if(address <= 0x97FF) {
            //TODO
        } else if (address <= 0x9BFF) {
            //TODO
        } else if (address <= 0x9FFF) {
            //TODO
        } else if (address <= 0xBFFF) {
            cart.writeRam(address - 0xA000, value); //TODO check
        } else if (address <= 0xDFFF) {
            work[address - 0xC000] = value; //TODO check
        } else if (address <= 0xFDFF) {
            //TODO
        } else if (address <= 0xFE9F) {
            //TODO
        } else if (address <= 0xFEFF) {

            //TODO probably fail is needed

        } else if (address <= 0xFF7F) {
            //TODO
        } else if (address <= 0xFFFE) {
            //TODO
        } else if (address == 0xFFFF){
            //TODO
        }

        throw new InvalidMemoryWriteLocationException();
    }

    public void decrement(char address) throws InvalidMemoryReadLocationException, IOException, InvalidMemoryWriteLocationException {
        write(address, (byte) (read(address) - 1));
    }

    public void increment(char address) throws InvalidMemoryReadLocationException, IOException, InvalidMemoryWriteLocationException {
        write(address, (byte) (read(address) + 1));
    }
}
