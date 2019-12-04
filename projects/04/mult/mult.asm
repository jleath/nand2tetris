// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

@R0
D=M
@x
M=D

@R1
D=M
@y
M=D

@product
M=0

(LOOP)
@y
D=M
@STOP
D;JEQ
@y
M=M-1
@x
D=M
@product
M=M+D
@LOOP
0;JMP

(STOP)
@product
D=M
@R2
M=D

(END)
@END
0;JMP
