.class public MinhaClasse
.super java/lang/Object

.field private static idade I
.field private static nome Ljava/lang/String;
.field private static texto Ljava/lang/String;
.field private static sobrenome Ljava/lang/String;
.field private static bonus F
.field private static salario F
.field private static salarioLiquido F

.method public static main([Ljava/lang/String;)V
    .limit stack 50
    .limit locals 50

L_repeat_start_0:
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Digite o seu nome: "
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/next()Ljava/lang/String;
putstatic MinhaClasse/nome Ljava/lang/String;
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Digite o seu sobrenome"
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/next()Ljava/lang/String;
putstatic MinhaClasse/sobrenome Ljava/lang/String;
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Digite a sua idade: "
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/nextInt()I
putstatic MinhaClasse/idade I
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Digite o salario: "
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/nextFloat()F
putstatic MinhaClasse/salario F
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Digite o bonus: "
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/nextFloat()F
putstatic MinhaClasse/bonus F
getstatic MinhaClasse/idade I
iconst_0
if_icmpgt L_rel_true_1
iconst_0
goto L_rel_end_2
L_rel_true_1:
iconst_1
L_rel_end_2:
getstatic MinhaClasse/salario F
iconst_0
i2f
fcmpl
ifgt L_rel_true_3
iconst_0
goto L_rel_end_4
L_rel_true_3:
iconst_1
L_rel_end_4:
iand
getstatic MinhaClasse/bonus F
iconst_0
i2f
fcmpl
ifge L_rel_true_5
iconst_0
goto L_rel_end_6
L_rel_true_5:
iconst_1
L_rel_end_6:
iand
ifeq L_if_else_7
getstatic MinhaClasse/salario F
getstatic MinhaClasse/salario F
ldc 0.11
fmul
fsub
getstatic MinhaClasse/bonus F
ldc 109.0
fsub
fadd
putstatic MinhaClasse/salarioLiquido F
new java/lang/StringBuilder
dup
invokespecial java/lang/StringBuilder/<init>()V
new java/lang/StringBuilder
dup
invokespecial java/lang/StringBuilder/<init>()V
new java/lang/StringBuilder
dup
invokespecial java/lang/StringBuilder/<init>()V
getstatic MinhaClasse/nome Ljava/lang/String;
invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;
ldc " "
invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;
invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;
invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;
getstatic MinhaClasse/sobrenome Ljava/lang/String;
invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;
invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;
invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;
ldc ":"
invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;
invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;
putstatic MinhaClasse/texto Ljava/lang/String;
getstatic java/lang/System/out Ljava/io/PrintStream;
getstatic MinhaClasse/texto Ljava/lang/String;
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Salario liquido: "
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
getstatic java/lang/System/out Ljava/io/PrintStream;
getstatic MinhaClasse/salario F
invokevirtual java/io/PrintStream/println(F)V
goto L_if_end_8
L_if_else_7:
L_if_end_8:
getstatic MinhaClasse/idade I
iconst_0
if_icmple L_rel_true_9
iconst_0
goto L_rel_end_10
L_rel_true_9:
iconst_1
L_rel_end_10:
getstatic MinhaClasse/salario F
iconst_0
i2f
fcmpl
ifle L_rel_true_11
iconst_0
goto L_rel_end_12
L_rel_true_11:
iconst_1
L_rel_end_12:
ior
getstatic MinhaClasse/bonus F
iconst_0
i2f
fcmpl
iflt L_rel_true_13
iconst_0
goto L_rel_end_14
L_rel_true_13:
iconst_1
L_rel_end_14:
ior
ifeq L_repeat_start_0
    return
.end method
