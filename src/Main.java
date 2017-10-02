
public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Gameboy.class <filename>");
            return;
        }
        try {
            Cartridge c = new Cartridge(args[0]);
            Memory m = new Memory(c);
            CPU cpu = new CPU(m);

            cpu.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
