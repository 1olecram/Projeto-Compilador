.class public MinhaClasse
.super java/lang/Object

.field private static a F
.field private static b F
.field private static c F
.field private static maior F

.method public static main([Ljava/lang/String;)V
    .limit stack 50
    .limit locals 50

getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Digite um número"
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/nextFloat()F
putstatic MinhaClasse/a F
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Digite outro número: "
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/nextFloat()F
putstatic MinhaClasse/b F
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "Digite mais um número: "
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
new java/util/Scanner
dup
getstatic java/lang/System/in Ljava/io/InputStream;
invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
invokevirtual java/util/Scanner/nextFloat()F
putstatic MinhaClasse/c F
ldc 0.0
putstatic MinhaClasse/maior F
getstatic MinhaClasse/a F
getstatic MinhaClasse/b F
fcmpl
ifgt L_rel_true_0
iconst_0
goto L_rel_end_1
L_rel_true_0:
iconst_1
L_rel_end_1:
getstatic MinhaClasse/a F
getstatic MinhaClasse/c F
fcmpl
ifgt L_rel_true_2
iconst_0
goto L_rel_end_3
L_rel_true_2:
iconst_1
L_rel_end_3:
iand
ifeq L_if_else_4
getstatic MinhaClasse/a F
putstatic MinhaClasse/maior F
goto L_if_end_5
L_if_else_4:
getstatic MinhaClasse/b F
getstatic MinhaClasse/c F
fcmpl
ifgt L_rel_true_6
iconst_0
goto L_rel_end_7
L_rel_true_6:
iconst_1
L_rel_end_7:
ifeq L_if_else_8
getstatic MinhaClasse/b F
putstatic MinhaClasse/maior F
goto L_if_end_9
L_if_else_8:
getstatic MinhaClasse/c F
putstatic MinhaClasse/maior F
L_if_end_9:
L_if_end_5:
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "O maior número e: "
invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
getstatic java/lang/System/out Ljava/io/PrintStream;
getstatic MinhaClasse/maior F
invokevirtual java/io/PrintStream/println(F)V
    return
.end method
