/**
 * Created by Goncalo on 30/09/2017.
 */
public class CPU {

    class Register16 {
        char value;

        public Register16(char value) {
            this.value = value;
        }

        public void increment() {
            value++;
        }

        public void decrement() {
            value--;
        }

        public char getAndIncrement() {
            char r = value;
            value++;
            return r;
        }
    }

    class Register8 {
        byte value;

        public Register8(byte value) {
            this.value = value;
        }

        public void increment() {
            value++;
        }

        public void decrement() {
            value--;
        }

        public boolean rotateLeft(boolean rotateFromEdge) {
            boolean r = value < 0;
            value <<= 1;
            if (rotateFromEdge && r || f.getCarry())
                value |= 1;

            return r;
        }

        public boolean rotateRight(boolean rotateFromEdge) {
            boolean r = (value % 2) == 1;
            value <<= 1;
            if (rotateFromEdge && r || f.getCarry())
                value |= 1 << 7;

            return r;
        }
    }

    class Flags extends Register8 {
        public Flags() {
            super((byte) 0);
        }

        public boolean getZero() {
            return (value & (1 << 7)) > 0;
        }

        public boolean getSubtract() {
            return (value & (1 << 6)) > 0;
        }

        public boolean getHalfCarry() {
            return (value & (1 << 5)) > 0;
        }

        public boolean getCarry() {
            return (value & (1 << 4)) > 0;
        }

        public void set(boolean b, int i) {
            if (b) {
                value |= 1 << i;
            } else {
                value &= 0xFF - (1 << i);
            }
        }
    }

    Register8 a, b, c, d, e, h, l;
    Flags f;
    Register16 sp, pc;
    Byte[] memory;

    long time;

    public CPU(Byte[] memory){
        this.memory = memory;
        a = new Register8((byte) 0); //accumulator
        f = new Flags(); //flags
        b = new Register8((byte) 0);
        c = new Register8((byte) 0);
        d = new Register8((byte) 0);
        e = new Register8((byte) 0);
        h = new Register8((byte) 0);
        l = new Register8((byte) 0);
        sp = new Register16((char) 0); //stack pointer
        pc = new Register16((char) 0); //program counter

        time = 0;
    }

    private char getAddressFromMemory() {
        return getAddress(memory[pc.getAndIncrement()], memory[pc.getAndIncrement()]);
    }

    private char getAddress(byte b, byte c) {
        return (char) (b * 256 + c);
    }

    private int getByteFromMemory() {
        return memory[pc.getAndIncrement()];
    }

    private char getWordFromMemory() {
        return getWord(memory[pc.getAndIncrement()], memory[pc.getAndIncrement()]);
    }

    private char getWord(byte top, byte bottom) {
        return (char) (top * 256 + bottom);
    }

    public void interpret() {
        switch (getByteFromMemory()) {
            case 0x00: nop(); break;
            case 0x01: loadRegisters16(b, c, getWordFromMemory()); break;
            case 0x02: loadMemory8(getAddress(b.value, c.value), a); break;
            case 0x03: increment(b, c); break;
            case 0x04: increment(b); break;
            case 0x05: decrement(b); break;
            case 0x06: loadRegister8(b, getByteFromMemory()); break;
            case 0x07: rotateLeftCarryAccumulator(); break;
            case 0x08: loadMemory16(getAddressFromMemory(), sp.value); break;
            case 0x09: add16(h, l, b, c); break;
            case 0x0A: loadRegister8(a, getAddress(b.value, c.value)); break;
            case 0x0B: decrement(b, c);
            case 0x0C: increment(c); break;
            case 0x0D: decrement(c); break;
            case 0x0E: loadRegister8(c, getByteFromMemory()); break;
            case 0x0F: rotateRightCarryAccumulator(); break;

            case 0x10: stop(); pc.getAndIncrement(); break;
            case 0x11: loadRegisters16(d, e, getWordFromMemory()); break;
            case 0x12: loadMemory8(getAddress(d.value, e.value), a); break;
            case 0x13: increment(d, e); break;
            case 0x14: increment(d); break;
            case 0x15: decrement(d); break;
            case 0x16: loadRegister8(d, getByteFromMemory()); break;
            case 0x17: rotateLeftAccumulator(); break;
            case 0x18: jumpRelative(getByteFromMemory()); break;
            case 0x19: add16(h, l, d, e); break;
            case 0x1A: loadRegister8(a, getAddress(d.value, e.value)); break;
            case 0x1B: decrement(d, e);
            case 0x1C: increment(e); break;
            case 0x1D: decrement(e); break;
            case 0x1E: loadRegister8(e, getByteFromMemory()); break;
            case 0x1F: rotateRightAccumulator(); break;

            case 0x20: jumpRelative(!f.getZero(), getByteFromMemory()); break;
            case 0x21: loadRegisters16(h, l, getWordFromMemory()); break;
            //case 0x22: loadMemory8(getAddress(h.value, l.value), a); break; //TODO LD (HL+),A
            case 0x23: increment(h, l); break;
            case 0x24: increment(h); break;
            case 0x25: decrement(h); break;
            case 0x26: loadRegister8(h, getByteFromMemory()); break;
            case 0x27: decimalAdjustAccumulator(); break;
            case 0x28: jumpRelative(f.getZero(), getByteFromMemory()); break;
            case 0x29: add16(h, l, h, l); break;
            //case 0x1A: loadRegister8(a, getAddress(h.value, l.value)); break; //TODO LD A,(HL+)
            case 0x2B: decrement(h, l);
            case 0x2C: increment(l); break;
            case 0x2D: decrement(l); break;
            case 0x2E: loadRegister8(l, getByteFromMemory()); break;
            case 0x2F: complementAccumulator(); break;

            case 0x30: jumpRelative(!f.getCarry(), getByteFromMemory()); break;
            case 0x31: loadRegisters16(sp, getWordFromMemory()); break;
            //case 0x22: loadMemory8(getAddress(h.value, l.value), a); break; //TODO LD (HL-),A
            case 0x33: increment(sp); break;
            case 0x34: increment(getAddress(h.value, l.value)); break;
            case 0x35: decrement(getAddress(h.value, l.value)); break;
            case 0x36: loadRegister8(getAddress(h.value, l.value), getByteFromMemory()); break;
            case 0x37: setCarryFlag(); break;
            case 0x38: jumpRelative(f.getCarry(), getByteFromMemory()); break;
            case 0x39: add16(h, l, sp); break;
            //case 0x1A: loadRegister8(a, getAddress(h.value, l.value)); break; //TODO LD A,(HL-)
            case 0x3B: decrement(sp);
            case 0x3C: increment(a); break;
            case 0x3D: decrement(a); break;
            case 0x3E: loadRegister8(a, getByteFromMemory()); break;
            case 0x3F: complementCarryFlag(); break;

            case 0x40: loadRegister8(b, b); break;
            case 0x41: loadRegister8(b, c); break;
            case 0x42: loadRegister8(b, d); break;
            case 0x43: loadRegister8(b, e); break;
            case 0x44: loadRegister8(b, h); break;
            case 0x45: loadRegister8(b, l); break;
            case 0x46: loadRegister8(b, getAddress(h.value, l.value)); break;
            case 0x47: loadRegister8(b, a); break;
            case 0x48: loadRegister8(c, b); break;
            case 0x49: loadRegister8(c, c); break;
            case 0x4A: loadRegister8(c, d); break;
            case 0x4B: loadRegister8(c, e); break;
            case 0x4C: loadRegister8(c, h); break;
            case 0x4D: loadRegister8(c, l); break;
            case 0x4E: loadRegister8(c, getAddress(h.value, l.value)); break;
            case 0x4F: loadRegister8(c, a); break;

            case 0x50: loadRegister8(d, b); break;
            case 0x51: loadRegister8(d, c); break;
            case 0x52: loadRegister8(d, d); break;
            case 0x53: loadRegister8(d, e); break;
            case 0x54: loadRegister8(d, h); break;
            case 0x55: loadRegister8(d, l); break;
            case 0x56: loadRegister8(d, getAddress(h.value, l.value)); break;
            case 0x57: loadRegister8(d, a); break;
            case 0x58: loadRegister8(e, b); break;
            case 0x59: loadRegister8(e, c); break;
            case 0x5A: loadRegister8(e, d); break;
            case 0x5B: loadRegister8(e, e); break;
            case 0x5C: loadRegister8(e, h); break;
            case 0x5D: loadRegister8(e, l); break;
            case 0x5E: loadRegister8(e, getAddress(h.value, l.value)); break;
            case 0x5F: loadRegister8(e, a); break;

            case 0x60: loadRegister8(h, b); break;
            case 0x61: loadRegister8(h, c); break;
            case 0x62: loadRegister8(h, d); break;
            case 0x63: loadRegister8(h, e); break;
            case 0x64: loadRegister8(h, h); break;
            case 0x65: loadRegister8(h, l); break;
            case 0x66: loadRegister8(h, getAddress(h.value, l.value)); break;
            case 0x67: loadRegister8(h, a); break;
            case 0x68: loadRegister8(l, b); break;
            case 0x69: loadRegister8(l, c); break;
            case 0x6A: loadRegister8(l, d); break;
            case 0x6B: loadRegister8(l, e); break;
            case 0x6C: loadRegister8(l, h); break;
            case 0x6D: loadRegister8(l, l); break;
            case 0x6E: loadRegister8(l, getAddress(h.value, l.value)); break;
            case 0x6F: loadRegister8(l, a); break;

            case 0x70: loadMemory8(getAddress(h.value, l.value), b); break;
            case 0x71: loadMemory8(getAddress(h.value, l.value), c); break;
            case 0x72: loadMemory8(getAddress(h.value, l.value), d); break;
            case 0x73: loadMemory8(getAddress(h.value, l.value), e); break;
            case 0x74: loadMemory8(getAddress(h.value, l.value), h); break;
            case 0x75: loadMemory8(getAddress(h.value, l.value), l); break;
            case 0x76: halt(); break;
            case 0x77: loadMemory8(getAddress(h.value, l.value), a); break;
            case 0x78: loadRegister8(a, b); break;
            case 0x79: loadRegister8(a, c); break;
            case 0x7A: loadRegister8(a, d); break;
            case 0x7B: loadRegister8(a, e); break;
            case 0x7C: loadRegister8(a, h); break;
            case 0x7D: loadRegister8(a, l); break;
            case 0x7E: loadRegister8(a, getAddress(h.value, l.value)); break;
            case 0x7F: loadRegister8(a, a); break;

            case 0x80: add8(b); break;
            case 0x81: add8(c); break;
            case 0x82: add8(d); break;
            case 0x83: add8(e); break;
            case 0x84: add8(h); break;
            case 0x85: add8(l); break;
            case 0x86: add8(getAddress(h.value, l.value)); break;
            case 0x87: add8(a); break;
        }
    }

    private void add8(char address) {
        add8(memory[address]);
    }

    private void add8(Register8 b){
        add8(b.value);
    }

    private void add8(byte b) {
        char r = (char) (a.value + b);
        f.set(r > 255, 4);

        f.set((a.value & 0x0F) + (b & 0x0F) > 0x0F, 5);

        f.set(false, 6);

        f.set(r % 256 == 0, 7);

        a.value = (byte) (r % 256);

        time += 4;
    }

    private void halt() {
        //TODO what does this do?

        time += 4;
    }

    private void loadRegister8(Register8 dest, Register8 orig) {
        dest.value = orig.value;

        time += 4;
    }

    private void complementAccumulator() {
        //TODO make sure this is correct

        a.value = (byte) -a.value;
        f.set(true, 5);
        f.set(true, 6);
        time += 4;
    }

    private void decimalAdjustAccumulator() {
        //TODO understand what this does
        
        time += 4;
    }

    private void complementCarryFlag() {
        f.set(!f.getCarry(), 4);
        f.set(false, 5);
        f.set(false, 6);
        time += 4;
    }

    private void decrement(Register16 sp) {
        sp.decrement();
        time += 8;
    }

    private void add16(Register8 h, Register8 l, Register16 sp) {
        byte high = (byte) ((sp.value >> 8) & 0xFF);
        byte low = (byte) (sp.value & 0xFF);
        add16(h, l, high, low);
    }

    private void setCarryFlag() {
        f.set(true, 4);
        f.set(false, 5);
        f.set(false, 6);
        time += 4;
    }

    private void loadRegister8(char address, byte byteFromMemory) {
        memory[address] = byteFromMemory;
        time += 12;
    }

    private void decrement(char address) {
        byte value = memory[address]--;
        f.set((value & 0x0F) == 0, 5);
        f.set(true, 6);
        f.set(value == 0, 7);
        time += 12;
    }

    private void increment(char address) {
        byte value = memory[address]++;
        f.set((value & 0x0F) == 0, 5);
        f.set(false, 6);
        f.set(value == 0, 7);
        time += 12;
    }

    private void increment(Register16 sp) {
        sp.increment();
        time += 8;
    }

    private void loadRegisters16(Register16 sp, char wordFromMemory) {
        sp.value = wordFromMemory;
        time += 12;
    }

    private void jumpRelative(byte byteFromMemory) {
        jumpRelative(true, byteFromMemory);
    }

    private void jumpRelative(boolean b, byte byteFromMemory) {
        if (b) {
            pc.value += byteFromMemory;
            time += 12;
        } else {
            time += 8;
        }

    }

    private void rotateRightAccumulator() {
        f.set(a.rotateRight(false), 4);
        f.set(false, 5);
        f.set(false, 6);
        f.set(false, 7);

        time += 4;
    }

    private void rotateLeftAccumulator() {
        f.set(a.rotateLeft(false), 4);
        f.set(false, 5);
        f.set(false, 6);
        f.set(false, 7);

        time += 4;
    }

    private void stop() {
        //TODO what does this do?

        time += 4;
    }

    private void rotateRightCarryAccumulator() {
        f.set(a.rotateRight(true), 4);
        f.set(false, 5);
        f.set(false, 6);
        f.set(false, 7);

        time += 4;
    }

    private void decrement(Register8 high, Register8 low) {
        low.decrement();
        if (low.value == -128)
            high.decrement();

        time += 8;
    }

    private void add16(Register8 h, Register8 l, Register8 b, Register8 c) {
        add16(h, l, b.value, c.value);
    }

    private void add16(Register8 h, Register8 l, byte b, byte c){
        char low = (char) (((char) l.value) + ((char) c));
        char high = (char) (low>255?1:0);
        low %= 256;
        high += b;
        high += h.value;
        high %= 256;
        h.value = (byte) high;
        l.value = (byte) low;


        //TODO set correct flags
        f.set(false, 4);
        f.set(false, 5);
        f.set(false, 6);


        time += 8;
    }

    private void loadMemory16(char addressFromMemory, char value) {
        memory[addressFromMemory] = (byte) ((value >> 8) & 0xFF);
        memory[addressFromMemory + 1] = (byte) (value & 0xFF);

        time += 20;
    }

    private void rotateLeftCarryAccumulator() {
        f.set(a.rotateLeft(true), 4);
        f.set(false, 5);
        f.set(false, 6);
        f.set(false, 7);

        time += 4;
    }

    private void loadRegister8(Register8 b, char memoryLocation) {
        loadRegister8(b, memory[memoryLocation]);
    }

    private void loadRegister8(Register8 b, byte byteFromMemory) {
        b.value = byteFromMemory;

        time += 8;
    }

    private void decrement(Register8 reg) {
        reg.decrement();

        f.set(reg.value == 0, 7);
        f.set(true, 6);
        f.set((reg.value & 0x0F) == 0x0F, 5);

        time += 4;
    }

    private void increment(Register8 b, Register8 c) {
        c.increment();
        if (c.value == 0)
            b.increment();

        time += 8;
    }

    private void increment(Register8 reg) {
        reg.increment();

        f.set(reg.value == 0, 7);
        f.set(false, 6);
        f.set((reg.value & 0x0F) == 0, 5);

        time += 4;
    }

    private void loadMemory8(char address, Register8 a) {
        memory[address] = a.value;
        time += 8;
    }

    private void loadRegisters16(Register8 b, Register8 c, char wordFromMemory) {
        b.value = (byte) ((wordFromMemory >> 8) & 0xFF);
        c.value = (byte) ((wordFromMemory) & 0xFF);
        time += 12;
    }

    private void nop() {
        time += 4;
    }
}
