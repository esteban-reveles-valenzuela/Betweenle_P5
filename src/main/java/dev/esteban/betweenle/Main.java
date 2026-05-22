package dev.esteban.betweenle;

import dev.esteban.model.Juego;
import java.text.Collator;
import java.util.*;

public class Main
{
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args)
    {
        Juego juego = new Juego();

        System.out.println("=== BETWEENLE ===");

        String idioma = pedirIdioma();
        int longitud = pedirLongitud();
        int intentos = pedirIntentos();

        while (!juego.configurar(idioma, longitud, intentos))
        {
            System.out.println("\nNo hay palabras de esa longitud en ese idioma.");
            System.out.println("Vuelve a escoger una longitud.");
            longitud = pedirLongitud();
        }

        System.out.println("\nJuego listo.");
        System.out.println("Idioma: " + (idioma.equals("es") ? "Español" : "Inglés"));
        System.out.println("Longitud objetivo: " + longitud);
        System.out.println("Intentos: " + intentos);

        while (!juego.termino())
        {
            mostrarEstado(juego);

            System.out.println("\n1) Intentar palabra");
            System.out.println("2) Pedir pista");
            System.out.println("3) Ver historial");
            System.out.println("4) Rendirse");
            System.out.println("5) Salir");

            System.out.print("Opción: ");

            String op = sc.nextLine().trim();

            if (op.equals("2"))
            {
                pedirYMostrarPista(juego);
                continue;
            }

            if (op.equals("3"))
            {
                mostrarHistorial(juego);
                continue;
            }

            if (op.equals("4"))
            {
                juego.rendirse();
                break;
            }
            if (op.equals("5"))
            {
                return;
            }

            System.out.print("Palabra: ");

            String palabra = sc.nextLine().trim().toLowerCase(Locale.ROOT);
            String res = juego.intentar(palabra);

            if (Juego.NO_EN_DICCIONARIO.equals(res))
            {
                System.out.println("Esa palabra no está en el diccionario.");
                System.out.print("¿La quieres agregar? (s/n): ");
                String resp = sc.nextLine().trim().toLowerCase(Locale.ROOT);

                if (resp.equals("s"))
                {
                    System.out.println(juego.agregarPalabra(palabra));
                    if (palabra.length() == juego.getLongitud())
                    {
                        res = juego.intentar(palabra);
                    }
                    else
                    {
                        res = "La palabra quedó agregada, " +
                            "pero no sirve para esta ronda " +
                            "porque no tiene " + juego.getLongitud() +
                            " letras.";
                    }
                }
                else
                {
                    res = "Intento descartado.";
                }
            }

            System.out.println(res);

            if (!juego.termino())
            {
                System.out.println();
            }
        }

        System.out.println("\n=== FIN DEL JUEGO ===");

        if (juego.gano())
        {
            System.out.println("Ganaste!!.");
        }
        else if (juego.rindio())
        {
            System.out.println("Ni modo....\n" +
                "la palabra secreta era: " + juego.getPalabraSecreta());
        }
        else
        {
            System.out.println("Perdiste. Se acabaron los intentos.\n" +
                "la palabra secreta era: " + juego.getPalabraSecreta());
        }

        System.out.println("\nHistorial final:");
        mostrarHistorial(juego);
    }

    private static String pedirIdioma()
    {
        while (true)
        {
            System.out.println("Idioma:");
            System.out.println("1) Español");
            System.out.println("2) Inglés");

            System.out.print("> ");

            String op = sc.nextLine().trim();

            if (op.equals("1"))
            {
                return "es";
            }

            if (op.equals("2"))
            {
                return "en";
            }

            System.out.println("Opción inválida.");
        }
    }

    private static int pedirLongitud()
    {
        while (true)
        {
            System.out.println("\nDificultad / longitud:");
            System.out.println("1) Fácil (5 letras)");
            System.out.println("2) Intermedio (6 letras)");
            System.out.println("3) Difícil (longitud personalizada)");
            System.out.print("> ");

            String op = sc.nextLine().trim();

            if (op.equals("1"))
            {
                return 5;
            }

            if (op.equals("2"))
            {
                return 6;
            }

            if (op.equals("3"))
            {
                int n = leerEntero("¿Cuántas letras? ");

                if (n >= 7)
                {
                    return n;
                }

                System.out.println("Pon una longitud válida.");
            }
            else
            {
                System.out.println("Opción inválida.");
            }
        }
    }

    private static int pedirIntentos()
    {
        while (true)
        {
            System.out.println("\nIntentos:");
            System.out.println("1) 10");
            System.out.println("2) 12");
            System.out.println("3) 14");
            System.out.print("> ");

            String op = sc.nextLine().trim();

            if (op.equals("1"))
            {
                return 10;
            }

            if (op.equals("2"))
            {
                return 12;
            }

            if (op.equals("3"))
            {
                return 14;
            }

            System.out.println("Opción inválida.");
        }
    }

    private static int leerEntero(String texto)
    {
        while (true)
        {
            try
            {
                System.out.print(texto);
                return Integer.parseInt(sc.nextLine().trim());
            }
            catch (NumberFormatException e)
            {
                System.out.println("Eso no es un número.");
            }
        }
    }

    private static void pedirYMostrarPista(Juego juego)
    {
        System.out.println("\nTipos de pista:");
        System.out.println("1) Recorrer 1% arriba");
        System.out.println("2) Recorrer 1% abajo");
        System.out.println("3) Letra inicial");
        System.out.print("> ");

        String op = sc.nextLine().trim();

        int tipo;

        switch (op)
        {
            case "1":
                tipo = 1;
            break;

            case "2":
                tipo = 2;
            break;

            case "3":
                tipo = 3;
            break;

            default:
                tipo = -1;
            break;
        }

        System.out.println
        (
            juego.pedirPista(tipo)
        );
    }

    private static void mostrarEstado(Juego juego)
    {
        System.out.println("==================================");
        System.out.println("Intentos hechos: " + juego.getIntentosHechos());
        System.out.println("Intentos restantes: " + juego.getIntentosRestantes());
        System.out.println("Longitud objetivo: " + juego.getLongitud());

        String limSup = juego.getLimiteSup();
        String limInf = juego.getLimiteInf();

        boolean mostrarPorcentajes = limSup != null || limInf != null;

        String txtSup;
        if (limSup == null)
        {
            txtSup = "a".repeat(juego.getLongitud());
        }
        else
        {
            txtSup = limSup;
        }

        String txtInf;
        if (limInf == null)
        {
            txtInf = "z".repeat(juego.getLongitud());
        }
        else
        {
            txtInf = limInf;
        }

        if (mostrarPorcentajes)
        {
            txtSup += " - " + String.format("%.2f%%", juego.getPorcentajeArriba());
            txtInf += " - " + String.format("%.2f%%", juego.getPorcentajeAbajo());
        }

        System.out.println("Límite arriba: " + txtSup);
        System.out.println("Límite abajo: " + txtInf);

        System.out.print("Letras usadas: ");
        HashSet<Character> letras = juego.getLetrasUsadas();

        if (letras.isEmpty())
        {
            System.out.println("ninguna");
        }
        else
        {
            Collator col = Collator.getInstance(new Locale("es", "MX"));
            col.setStrength(Collator.SECONDARY);
            Comparator comparador = (a, b) -> col.compare(a.toString(), b.toString());

            ArrayList<Character> ordenadas = new ArrayList<>(letras);
            ordenadas.sort(comparador);

            Iterator<Character> it = ordenadas.iterator();

            while (it.hasNext())
            {
                System.out.print(it.next());
                if (it.hasNext())
                {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
        System.out.println("==================================");
    }

    private static void mostrarHistorial(Juego juego)
    {
        ArrayList<String> historial = juego.getHistorial();

        if (historial.isEmpty())
        {
            System.out.println("Sin intentos.");
            return;
        }

        Iterator<String> it = historial.iterator();

        while (it.hasNext())
        {
            System.out.println(it.next());
        }
    }
}