.class public Teste1
.super java/lang/Object

.field private static base I
.field private static altura I
.field private static area F

.method public static main([Ljava/lang/String;)V
    .limit stack 50
    .limit locals 50

getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Digite o valor da base:"
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/nextInt()I
putstatic Teste1/base I
getstatic Teste1/base I
getstatic Teste1/altura I
imul
i2f
ldc 2.0
fdiv
putstatic Teste1/area F
    return
.end method
