import java.io.IOException;


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
    Memory memory;
    boolean interruptsEnabled;

    long time;

    public CPU(Memory memory){
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

        interruptsEnabled = false; //check if interrupts start disabled

        time = 0;
    }

    public void run() throws InvalidMemoryReadLocationException, IOException, InvalidMemoryWriteLocationException, InstructionNotImplementedException, InvalidInstructionException {
        while (true) {
            interpret(getByteFromMemory());
        }

    }

    private byte popByteFromStack() throws InvalidMemoryReadLocationException, IOException {
        return memory.read(sp.value++);
    }

    private void pushByteToStack(byte b) throws InvalidMemoryWriteLocationException {
        memory.write(sp.value--, b);
    }

    private char getAddressFromMemory() throws InvalidMemoryReadLocationException, IOException {
        return getAddress(memory.read(pc.getAndIncrement()), memory.read(pc.getAndIncrement()));
    }

    private char getHighRamAddress(byte b) {
        return (char) (b + 0x9900);
    }

    private char getAddress(byte b, byte c) {
        return (char) (b * 256 + c);
    }

    private int getByteFromMemory() throws InvalidMemoryReadLocationException, IOException {
        return memory.read(pc.getAndIncrement());
    }

    private char getWordFromMemory() throws InvalidMemoryReadLocationException, IOException {
        return getWord(memory.read(pc.getAndIncrement()), memory.read(pc.getAndIncrement()));
    }

    private char getWord(byte top, byte bottom) {
        return (char) (top * 256 + bottom);
    }

    public void interpret(int instruction) throws InstructionNotImplementedException, InvalidInstructionException, InvalidMemoryReadLocationException, IOException, InvalidMemoryWriteLocationException {
        switch (instruction) {
            case 0x00: nop(); break;
            case 0x01: loadRegisters16(b, c, getWordFromMemory()); break;
            case 0x02: loadMemory8(getAddress(b.value, c.value), a); break;
            case 0x03: increment(b, c); break;
            case 0x04: increment(b); break;
            case 0x05: decrement(b); break;
            case 0x06: loadRegister8(b, (byte) getByteFromMemory()); break;
            case 0x07: rotateLeftCarryAccumulator(); break;
            case 0x08: loadMemory16(getAddressFromMemory(), sp.value); break;
            case 0x09: add16(h, l, b, c); break;
            case 0x0A: loadRegister8(a, getAddress(b.value, c.value)); break;
            case 0x0B: decrement(b, c);
            case 0x0C: increment(c); break;
            case 0x0D: decrement(c); break;
            case 0x0E: loadRegister8(c, (byte) getByteFromMemory()); break;
            case 0x0F: rotateRightCarryAccumulator(); break;

            case 0x10: stop(); pc.getAndIncrement(); break;
            case 0x11: loadRegisters16(d, e, getWordFromMemory()); break;
            case 0x12: loadMemory8(getAddress(d.value, e.value), a); break;
            case 0x13: increment(d, e); break;
            case 0x14: increment(d); break;
            case 0x15: decrement(d); break;
            case 0x16: loadRegister8(d, (byte) getByteFromMemory()); break;
            case 0x17: rotateLeftAccumulator(); break;
            case 0x18: jumpRelative((byte) getByteFromMemory()); break;
            case 0x19: add16(h, l, d, e); break;
            case 0x1A: loadRegister8(a, getAddress(d.value, e.value)); break;
            case 0x1B: decrement(d, e);
            case 0x1C: increment(e); break;
            case 0x1D: decrement(e); break;
            case 0x1E: loadRegister8(e, (byte) getByteFromMemory()); break;
            case 0x1F: rotateRightAccumulator(); break;

            case 0x20: jumpRelative(!f.getZero(), (byte) getByteFromMemory()); break;
            case 0x21: loadRegisters16(h, l, getWordFromMemory()); break;
            case 0x22: loadIncrement(h, l, a.value);
            case 0x23: increment(h, l); break;
            case 0x24: increment(h); break;
            case 0x25: decrement(h); break;
            case 0x26: loadRegister8(h, (byte) getByteFromMemory()); break;
            case 0x27: decimalAdjustAccumulator(); break;
            case 0x28: jumpRelative(f.getZero(), (byte) getByteFromMemory()); break;
            case 0x29: add16(h, l, h, l); break;
            case 0x2A: loadIncrement(a, h, l);
            case 0x2B: decrement(h, l);
            case 0x2C: increment(l); break;
            case 0x2D: decrement(l); break;
            case 0x2E: loadRegister8(l, (byte) getByteFromMemory()); break;
            case 0x2F: complementAccumulator(); break;

            case 0x30: jumpRelative(!f.getCarry(), (byte) getByteFromMemory()); break;
            case 0x31: loadRegisters16(sp, getWordFromMemory()); break;
            case 0x32: loadDecrement(h, l, a.value);
            case 0x33: increment(sp); break;
            case 0x34: increment(getAddress(h.value, l.value)); break;
            case 0x35: decrement(getAddress(h.value, l.value)); break;
            case 0x36: loadRegister8(getAddress(h.value, l.value), (byte) getByteFromMemory()); break;
            case 0x37: setCarryFlag(); break;
            case 0x38: jumpRelative(f.getCarry(), (byte) getByteFromMemory()); break;
            case 0x39: add16(h, l, sp); break;
            case 0x3A: loadDecrement(a, h, l);
            case 0x3B: decrement(sp);
            case 0x3C: increment(a); break;
            case 0x3D: decrement(a); break;
            case 0x3E: loadRegister8(a, (byte) getByteFromMemory()); break;
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
            case 0x86: add8(memory.read(getAddress(h.value, l.value))); break;
            case 0x87: add8(a); break;
            case 0x88: adc8(b); break;
            case 0x89: adc8(c); break;
            case 0x8A: adc8(d); break;
            case 0x8B: adc8(e); break;
            case 0x8C: adc8(h); break;
            case 0x8D: adc8(l); break;
            case 0x8E: adc8(memory.read(getAddress(h.value, l.value))); break;
            case 0x8F: adc8(a); break;

            case 0x90: sub8(b); break;
            case 0x91: sub8(c); break;
            case 0x92: sub8(d); break;
            case 0x93: sub8(e); break;
            case 0x94: sub8(h); break;
            case 0x95: sub8(l); break;
            case 0x96: sub8(memory.read(getAddress(h.value, l.value))); break;
            case 0x97: sub8(a); break;
            case 0x98: sbc8(b); break;
            case 0x99: sbc8(c); break;
            case 0x9A: sbc8(d); break;
            case 0x9B: sbc8(e); break;
            case 0x9C: sbc8(h); break;
            case 0x9D: sbc8(l); break;
            case 0x9E: sbc8(memory.read(getAddress(h.value, l.value))); break;
            case 0x9F: sbc8(a); break;

            case 0xA0: and8(b); break;
            case 0xA1: and8(c); break;
            case 0xA2: and8(d); break;
            case 0xA3: and8(e); break;
            case 0xA4: and8(h); break;
            case 0xA5: and8(l); break;
            case 0xA6: and8(memory.read(getAddress(h.value, l.value))); break;
            case 0xA7: and8(a); break;
            case 0xA8: xor8(b); break;
            case 0xA9: xor8(c); break;
            case 0xAA: xor8(d); break;
            case 0xAB: xor8(e); break;
            case 0xAC: xor8(h); break;
            case 0xAD: xor8(l); break;
            case 0xAE: xor8(memory.read(getAddress(h.value, l.value))); break;
            case 0xAF: xor8(a); break;

            case 0xB0: or8(b); break;
            case 0xB1: or8(c); break;
            case 0xB2: or8(d); break;
            case 0xB3: or8(e); break;
            case 0xB4: or8(h); break;
            case 0xB5: or8(l); break;
            case 0xB6: or8(memory.read(getAddress(h.value, l.value))); break;
            case 0xB7: or8(a); break;
            case 0xB8: cp8(b); break;
            case 0xB9: cp8(c); break;
            case 0xBA: cp8(d); break;
            case 0xBB: cp8(e); break;
            case 0xBC: cp8(h); break;
            case 0xBD: cp8(l); break;
            case 0xBE: cp8(memory.read(getAddress(h.value, l.value))); break;
            case 0xBF: cp8(a); break;

            case 0xC0: ret(!f.getZero()); break;
            case 0xC1: pop(b, c); break;
            case 0xC2: jumpAbsolute(!f.getZero(), getAddressFromMemory()); break;
            case 0xC3: jumpAbsolute(true, getAddressFromMemory()); break;
            case 0xC4: call(!f.getZero(), getAddressFromMemory()); break;
            case 0xC5: push(getWord(b.value, c.value)); break;
            case 0xC6: add8((char) getByteFromMemory()); break;
            case 0xC7: restart(0x00); break;
            case 0xC8: ret(f.getZero()); break;
            case 0xC9: absoluteReturn(); break;
            case 0xCA: jumpAbsolute(f.getZero(), getAddressFromMemory()); break;
            case 0xCB: prefixCB(getByteFromMemory()); break;
            case 0xCC: call(f.getZero(), getAddressFromMemory()); break;
            case 0xCD: call(true, getAddressFromMemory()); break;
            case 0xCE: adc8((char) getByteFromMemory());
            case 0xCF: restart(0x08); break;

            case 0xD0: ret(!f.getCarry()); break;
            case 0xD1: pop(d, e); break;
            case 0xD2: jumpAbsolute(!f.getCarry(), getAddressFromMemory()); break;
            case 0xD4: call(!f.getCarry(), getAddressFromMemory()); break;
            case 0xD5: push(getWord(d.value, e.value)); break;
            case 0xD6: sub8((char) getByteFromMemory()); break;
            case 0xD7: restart(0x10); break;
            case 0xD8: ret(f.getCarry()); break;
            case 0xD9: returnInterrupt(); break;
            case 0xDA: jumpAbsolute(f.getCarry(), getAddressFromMemory()); break;
            case 0xDC: call(f.getCarry(), getAddressFromMemory()); break;
            case 0xDE: sbc8((char) getByteFromMemory());
            case 0xDF: restart(0x18); break;

            case 0xE0: loadHigh(getByteFromMemory(), a);
            case 0xE1: pop(h, l); break;
            case 0xE2: loadMemory8(getHighRamAddress(c.value), a); //TODO check
            case 0xE5: push(getWord(h.value, l.value)); break;
            case 0xE6: and8((char) getByteFromMemory()); break;
            case 0xE7: restart(0x20); break;
            case 0xE8: add16(sp, getByteFromMemory()); break;
            case 0xE9: jumpFast(getAddress(h.value, l.value)); //TODO check JP (HL)
            case 0xEA: time += 8; loadMemory16(getAddressFromMemory(), (char) a.value); break;
            case 0xEE: xor8((char) getByteFromMemory());
            case 0xEF: restart(0x28); break;

            case 0xF0: loadHigh(a, getByteFromMemory());
            case 0xF1: pop(b, c); break;
            case 0xF2: loadRegister8(a, getHighRamAddress(c.value)); //TODO check
            case 0xF3: disableInterrupts(); break;
            case 0xF5: push(getWord(a.value, f.value)); break;
            case 0xF6: or8((char) getByteFromMemory()); break;
            case 0xF7: restart(0x30); break;
            case 0xF8: loadRegisters16(h, l, (char) (sp.value + getByteFromMemory())); //TODO check LD HL,SP+r8
            case 0xF9: loadRegisters16(sp, h, l); //TODO check LD SP,HL
            case 0xFA: time += 8; loadRegister8(a, memory.read(getAddress(h.value, l.value))); break;
            case 0xFB: enableInterrupts(); break;
            case 0xFE: cp8((char) getByteFromMemory()); break;
            case 0xFF: restart(0x38); break;

            default: throw new InvalidInstructionException();
        }
    }

    private void prefixCB(int instruction) throws InstructionNotImplementedException {
        switch (instruction) {
            default: throw new InstructionNotImplementedException();
        }
    }

    private void loadRegisters16(Register16 sp, Register8 h, Register8 l) {
        sp.value = getAddress(h.value, l.value);

        time += 8;
    }

    private void jumpFast(char address) {
        pc.value = address;

        time += 4;
    }

    private void loadDecrement(Register8 a, Register8 h, Register8 l) throws InvalidMemoryReadLocationException, IOException {
        a.value = memory.read(getAddress(h.value, l.value));
        l.decrement();
        if (l.value == Byte.MAX_VALUE)
            h.decrement();

        time += 8;
    }

    private void loadDecrement(Register8 h, Register8 l, byte value) throws InvalidMemoryWriteLocationException {
        memory.write(getAddress(h.value, l.value), value);
        l.decrement();
        if (l.value == Byte.MAX_VALUE)
            h.decrement();

        time += 8;
    }

    private void loadIncrement(Register8 a, Register8 h, Register8 l) throws InvalidMemoryReadLocationException, IOException {
        a.value = memory.read(getAddress(h.value, l.value));
        l.increment();
        if (l.value == Byte.MIN_VALUE)
            h.increment();

        time += 8;
    }

    private void loadIncrement(Register8 h, Register8 l, byte a) throws InvalidMemoryWriteLocationException {
        memory.write(getAddress(h.value, l.value), a);
        l.increment();
        if (l.value == Byte.MIN_VALUE)
            h.increment();

        time += 8;
    }

    private void loadHigh(Register8 a, int byteFromMemory) throws InvalidMemoryReadLocationException, IOException {
        a.value = memory.read((char) (0xFF00 + byteFromMemory));

        time += 12;
    }

    private void loadHigh(int byteFromMemory, Register8 a) throws InvalidMemoryWriteLocationException {
        memory.write((char) (0xFF00 + byteFromMemory), a.value);

        time += 12;
    }

    private void add16(Register16 sp, int byteFromMemory) {
        f.set(false, 7);
        f.set(false, 6);
        f.set(((sp.value & 0xFFF) + (byteFromMemory & 0xFFF)) > 0xFFF, 5);
        f.set(sp.value + byteFromMemory > 0xFFFF, 4);

        sp.value += byteFromMemory;

        time += 16;
    }

    private void enableInterrupts() {
        time += 4;

        interruptsEnabled = true;
    }

    private void disableInterrupts() {
        interruptsEnabled = false;

        time += 4;
    }

    private void call(boolean b, char address) throws InvalidMemoryWriteLocationException {
        if (b) {
            pc.increment();
            pushByteToStack((byte) (pc.value & 0xFF));
            pushByteToStack((byte) (pc.value & 0xFF00));
            pc.value = address;
            time += 24;
        } else {
            time += 12;
        }
    }

    private void returnInterrupt() throws InvalidMemoryReadLocationException, IOException {
        pc.value = getAddress(popByteFromStack(), popByteFromStack());

        interruptsEnabled = true;

        time += 16;
    }

    private void absoluteReturn() throws InvalidMemoryReadLocationException, IOException {
        pc.value = getAddress(popByteFromStack(), popByteFromStack());

        time += 16;
    }

    private void restart(int i) throws InvalidMemoryWriteLocationException {
        pushByteToStack((byte) (pc.value & 0xFF));
        pushByteToStack((byte) (pc.value & 0xFF00));

        pc.value = (char) i;

        time += 16;
    }

    private void push(char word) throws InvalidMemoryWriteLocationException {
        pushByteToStack((byte) (word & 0xFF));
        pushByteToStack((byte) (word & 0xFF00));

        time += 16;
    }

    private void pop(Register8 top, Register8 bottom) throws InvalidMemoryReadLocationException, IOException {
        top.value = popByteFromStack();
        bottom.value = popByteFromStack();

        time += 12;
    }

    private void ret(boolean b) throws InvalidMemoryReadLocationException, IOException {
        if (b) {
            pc.value = getAddress(popByteFromStack(), popByteFromStack());

            time += 20;
        } else {
            time += 8;
        }
    }

    private void jumpAbsolute(boolean b, char addressFromMemory) {
        if (b){
            pc.value = addressFromMemory;
            time += 16;
        } else {
            time += 12;
        }
    }

    private void cp8(char value) {
        time += 4;
        cp8((byte) value);
    }

    private void cp8(Register8 b) {
        cp8(b.value);
    }

    private void or8(char value) {
        time += 4;
        or8((byte) value);
    }

    private void or8(Register8 d) {
        or8(d.value);
    }

    private void xor8(char value) {
        time += 4;
        xor8((byte) value);
    }

    private void xor8(Register8 b) {
        xor8(b.value);
    }

    private void and8(char value) {
        time += 4;
        and8((byte) value);
    }

    private void and8(Register8 b) {
        and8(b.value);
    }

    private void sbc8(char value) {
        time += 4;
        sbc8((byte) value);
    }

    private void sbc8(Register8 b) {
        sbc8(b.value);
    }

    private void sub8(char value) {
        time += 4;
        sub8((byte) value);
    }

    private void sub8(Register8 b) {
        sub8(b.value);
    }

    private void adc8(Register8 b) {
        adc8(b.value);
    }

    private void adc8(char value) {
        time += 4;
        adc8((byte) value);
    }

    private void add8(char value) {
        time += 4;
        add8((byte) value);
    }

    private void add8(Register8 b){
        add8(b.value);
    }

    private void cp8(byte c) {
        f.set(a.value == c, 7);
        f.set(true, 6);
        f.set(((a.value & 0xF) - (c & 0xF)) < 0, 5);
        f.set(a.value < c, 4);

        time += 4;
    }

    private void or8(byte d) {
        a.value |= d;

        f.set(a.value == 0, 7);
        f.set(false, 6);
        f.set(false, 5);
        f.set(false, 4);

        time += 4;
    }

    private void xor8(byte d) {
        a.value ^= d;

        f.set(a.value == 0, 7);
        f.set(false, 6);
        f.set(false, 5);
        f.set(false, 4);

        time += 4;
    }

    private void and8(byte c) {
        a.value &= c;

        f.set(a.value == 0, 7);
        f.set(false, 6);
        f.set(true, 5);
        f.set(false, 4);

        time += 4;
    }

    private void sbc8(byte h) {
        //TODO
    }

    private void sub8(byte b) {
        //TODO
    }

    private void adc8(byte b) {
        //TODO
    }

    private void add8(byte b) {
        //check

        char r = (char) (a.value + b);
        f.set(r > 255, 4);

        f.set((a.value & 0x0F) + (b & 0x0F) > 0x0F, 5);

        f.set(false, 6);

        f.set(r % 256 == 0, 7);

        a.value = (byte) (r % 256);

        time += 4;
    }

    private void halt() {
        //TODO power down CPU until an interrupt occurs

        time += 4;
    }

    private void loadRegister8(Register8 dest, Register8 orig) {
        dest.value = orig.value;

        time += 4;
    }

    private void complementAccumulator() {
        //make sure this is correct

        a.value = (byte) -a.value;
        f.set(true, 5);
        f.set(true, 6);
        time += 4;
    }

    private void decimalAdjustAccumulator() {
        //check

        if ((a.value & 0xF) > 9) {
            a.value += 0x6;
        }
        if (((a.value & 0xF0) >> 4) > 9) {
            if (a.value + 0x60 > 255)
                f.set(true, 5);
            else
                f.set(false, 5);

            a.value += 0x60;
        }

        f.set(a.value == 0, 7);

        f.set(false, 4);

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

    private void loadRegister8(char address, byte byteFromMemory) throws InvalidMemoryWriteLocationException {
        memory.write(address, byteFromMemory);
        time += 12;
    }

    private void decrement(char address) throws InvalidMemoryReadLocationException, IOException, InvalidMemoryWriteLocationException {
        memory.decrement(address);
        byte value = memory.read(address);
        f.set((value & 0x0F) == 0, 5);
        f.set(true, 6);
        f.set(value == 0, 7);
        time += 12;
    }

    private void increment(char address) throws InvalidMemoryReadLocationException, IOException, InvalidMemoryWriteLocationException {
        memory.increment(address);
        byte value = memory.read(address);
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

        f.set((high + (b & 0xF) + (h.value & 0xF)) > 0xF, 5);

        high += b;
        high += h.value;
        high %= 256;
        h.value = (byte) high;
        l.value = (byte) low;


        f.set(false, 6);
        f.set(high > 255, 4);

        //check flags

        time += 8;
    }

    private void loadMemory16(char addressFromMemory, char value) throws InvalidMemoryWriteLocationException {
        memory.write(addressFromMemory, (byte) ((value >> 8) & 0xFF));
        memory.write((char) (addressFromMemory + 1), (byte) (value & 0xFF));

        time += 20;
    }

    private void rotateLeftCarryAccumulator() {
        f.set(a.rotateLeft(true), 4);
        f.set(false, 5);
        f.set(false, 6);
        f.set(false, 7);

        time += 4;
    }

    private void loadRegister8(Register8 b, char memoryLocation) throws InvalidMemoryReadLocationException, IOException {
        loadRegister8(b, memory.read(memoryLocation));
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

    private void loadMemory8(char address, Register8 a) throws InvalidMemoryWriteLocationException {
        memory.write(address, a.value);
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
