.class public Teste06
.super java/lang/Object

.field private static i I
.field private static qtd I
.field private static total I
.field private static nota F
.field private static media F
.field private static nome Ljava/lang/String;

.method public static main([Ljava/lang/String;)V
    .limit stack 50
    .limit locals 50

new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/nextInt()I
putstatic Teste06/qtd I
iconst_0
putstatic Teste06/i I
iconst_0
putstatic Teste06/total I
L_do_start_0:
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Informe nome e nota:"
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/next()Ljava/lang/String;
putstatic Teste06/nome Ljava/lang/String;
new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/nextFloat()F
putstatic Teste06/nota F
getstatic Teste06/nota F
ldc 7.0
fcmpl
ifge L_rel_true_1
iconst_0
goto L_rel_end_2
L_rel_true_1:
iconst_1
L_rel_end_2:
ifeq L_if_else_3
getstatic Teste06/total I
iconst_1
iadd
putstatic Teste06/total I
goto L_if_end_4
L_if_else_3:
getstatic Teste06/total I
iconst_0
iadd
putstatic Teste06/total I
L_if_end_4:
getstatic Teste06/nota F
iconst_0
i2f
fcmpl
ifne L_rel_true_5
iconst_0
goto L_rel_end_6
L_rel_true_5:
iconst_1
L_rel_end_6:
getstatic Teste06/nota F
iconst_0
i2f
fcmpl
iflt L_rel_true_7
iconst_0
goto L_rel_end_8
L_rel_true_7:
iconst_1
L_rel_end_8:
iconst_1
ixor
iand
ifeq L_if_else_9
getstatic Teste06/total I
i2f
ldc 1.0
fdiv
putstatic Teste06/media F
goto L_if_end_10
L_if_else_9:
L_if_end_10:
getstatic Teste06/i I
iconst_1
iadd
putstatic Teste06/i I
getstatic Teste06/i I
getstatic Teste06/qtd I
if_icmplt L_rel_true_11
iconst_0
goto L_rel_end_12
L_rel_true_11:
iconst_1
L_rel_end_12:
getstatic Teste06/qtd I
iconst_1
if_icmpge L_rel_true_13
iconst_0
goto L_rel_end_14
L_rel_true_13:
iconst_1
L_rel_end_14:
iand
ifne L_do_start_0
L_repeat_start_15:
getstatic java/lang/System/out Ljava/io/PrintStream;
new java/lang/StringBuilder
dup
invokespecial java/lang/StringBuilder/<init>()V
ldc "Aprovados: "
invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;
getstatic Teste06/total I
invokevirtual java/lang/StringBuilder/append(I)Ljava/lang/StringBuilder;
invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
getstatic Teste06/qtd I
iconst_1
isub
putstatic Teste06/qtd I
getstatic Teste06/qtd I
iconst_0
if_icmpeq L_rel_true_16
iconst_0
goto L_rel_end_17
L_rel_true_16:
iconst_1
L_rel_end_17:
ifeq L_repeat_start_15
    return
.end method
