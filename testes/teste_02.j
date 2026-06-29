.class public Teste2
.super java/lang/Object

.field private static a I

.method public static main([Ljava/lang/String;)V
    .limit stack 50
    .limit locals 50

getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Entre com o valor de a: "
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/nextInt()I
putstatic Teste2/a I
getstatic Teste2/a I
iconst_2
irem
iconst_0
if_icmpeq L_rel_true_0
iconst_0
goto L_rel_end_1
L_rel_true_0:
iconst_1
L_rel_end_1:
ifeq L_if_else_2
getstatic java/lang/System/out Ljava/io/PrintStream;
new java/lang/StringBuilder
dup
invokespecial java/lang/StringBuilder/<init>()V
getstatic Teste2/a I
invokevirtual java/lang/StringBuilder/append(I)Ljava/lang/StringBuilder;
ldc "e par"
invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;
invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
goto L_if_end_3
L_if_else_2:
getstatic java/lang/System/out Ljava/io/PrintStream;
new java/lang/StringBuilder
dup
invokespecial java/lang/StringBuilder/<init>()V
getstatic Teste2/a I
invokevirtual java/lang/StringBuilder/append(I)Ljava/lang/StringBuilder;
ldc "e impar"
invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;
invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
L_if_end_3:
    return
.end method
