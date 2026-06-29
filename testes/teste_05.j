.class public A
.super java/lang/Object

.field private static i I
.field private static soma F
.field private static altura F
.field private static media F
.field private static maior F
.field private static menor F
.field private static qtd I

.method public static main([Ljava/lang/String;)V
    .limit stack 50
    .limit locals 50

ldc 0.0
putstatic A/soma F
ldc 0.0
putstatic A/maior F
ldc 3.0
putstatic A/menor F
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Quantos dados deseja informar?"
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/nextInt()I
putstatic A/qtd I
getstatic A/qtd I
iconst_2
if_icmpge L_rel_true_0
iconst_0
goto L_rel_end_1
L_rel_true_0:
iconst_1
L_rel_end_1:
ifeq L_if_else_2
iconst_0
putstatic A/i I
L_do_start_4:
L_do_start_5:
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Altura: "
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/nextFloat()F
putstatic A/altura F
getstatic A/altura F
iconst_0
i2f
fcmpl
ifle L_rel_true_6
iconst_0
goto L_rel_end_7
L_rel_true_6:
iconst_1
L_rel_end_7:
getstatic A/altura F
ldc 2.5
fcmpl
ifge L_rel_true_8
iconst_0
goto L_rel_end_9
L_rel_true_8:
iconst_1
L_rel_end_9:
ior
ifne L_do_start_5
getstatic A/soma F
getstatic A/altura F
fadd
putstatic A/soma F
getstatic A/i I
iconst_1
iadd
putstatic A/i I
getstatic A/altura F
getstatic A/maior F
fcmpl
ifgt L_rel_true_10
iconst_0
goto L_rel_end_11
L_rel_true_10:
iconst_1
L_rel_end_11:
ifeq L_if_else_12
getstatic A/altura F
putstatic A/maior F
goto L_if_end_13
L_if_else_12:
L_if_end_13:
getstatic A/altura F
getstatic A/menor F
fcmpl
iflt L_rel_true_14
iconst_0
goto L_rel_end_15
L_rel_true_14:
iconst_1
L_rel_end_15:
ifeq L_if_else_16
goto L_if_end_17
L_if_else_16:
L_if_end_17:
getstatic A/i I
getstatic A/qtd I
if_icmplt L_rel_true_18
iconst_0
goto L_rel_end_19
L_rel_true_18:
iconst_1
L_rel_end_19:
ifne L_do_start_4
getstatic A/soma F
getstatic A/qtd I
i2f
fdiv
putstatic A/media F
getstatic java/lang/System/out Ljava/io/PrintStream;
new java/lang/StringBuilder
dup
invokespecial java/lang/StringBuilder/<init>()V
ldc "Media: "
invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;
getstatic A/media F
invokevirtual java/lang/StringBuilder/append(F)Ljava/lang/StringBuilder;
invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
goto L_if_end_3
L_if_else_2:
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Quantidade inválida."
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
L_if_end_3:
    return
.end method
