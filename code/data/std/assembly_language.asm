.text
    li t4, 8		#  (MOV, a, 8)
    li t5, 5		#  (MOV, b, 5)
    li t6, 3		#  (MOV, $6, 3)
    sub t0, t6, t4		#  (SUB, $0, $6, a)
    mv t1, t0		#  (MOV, c, $0)
    mul t2, t4, t5		#  (MUL, $1, a, b)
    addi t3, t5, 3		#  (ADD, $2, b, 3)
    sub t6, t1, t4		#  (SUB, $3, c, a)
    mul t0, t3, t6		#  (MUL, $4, $2, $3)
    sub t5, t2, t0		#  (SUB, $5, $1, $4)
    mv t4, t5		#  (MOV, result, $5)
    mv a0, t4		#  (RET, , result)

