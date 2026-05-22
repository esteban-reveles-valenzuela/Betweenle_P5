package dev.esteban.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

public class Juego
{
    public static final String NO_EN_DICCIONARIO = "__NO_EN_DICCIONARIO__";

    private File txtEs = new File("diccionario_es.txt");
    private File txtEn = new File("diccionario_en.txt");

    private Scanner lectorEs;
    private Scanner lectorEn;

    private Random rnd = new Random();

    private HashMap<String, Integer> diccEs = new HashMap<>();
    private HashMap<String, Integer> diccEn = new HashMap<>();

    private HashMap<String, Integer> diccActual = new HashMap<>();
    private ArrayList<String> ordenadas = new ArrayList<>();

    private HashSet<Character> letrasUsadas = new HashSet<>();
    private ArrayList<String> historial = new ArrayList<>();

    private String idioma;

    private String palabraSecreta;
    private int longitud;

    private int intentosMax;
    private int intentosHechos;

    private boolean gano;
    private boolean termino;

    private int indiceSup;
    private int indiceInf;
    private int indiceSecreta;

    private String limiteSup;
    private String limiteInf;

    private Comparator<String> comparador;

    public Juego()
    {
        cargarDiccionarios();
    }

    public boolean cargarDiccionarios()
    {
        diccEs.clear();
        diccEn.clear();

        try
        {
            lectorEs = new Scanner(txtEs);
            lectorEn = new Scanner(txtEn);
            while(lectorEs.hasNextLine())
            {
                String palabra = lectorEs.nextLine().trim().toLowerCase(Locale.ROOT);
                if(!palabra.isEmpty())
                {
                    diccEs.put(palabra, palabra.length());
                }
            }
            while(lectorEn.hasNextLine())
            {
                String palabra = lectorEn.nextLine().trim().toLowerCase(Locale.ROOT);
                if(!palabra.isEmpty())
                {
                    diccEn.put(palabra, palabra.length());
                }
            }
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    private String extraerPalabra(String linea)
    {
        int pos = linea.indexOf('|');
        if (pos < 0) pos = linea.indexOf(';');
        if (pos < 0) pos = linea.indexOf('\t');

        if (pos >= 0)
        {
            return linea.substring(0, pos).trim();
        }

        return linea.trim();
    }

    public boolean configurar(String idioma, int longitud, int intentosMax)
    {
        this.idioma = idioma;
        this.longitud = longitud;
        this.intentosMax = intentosMax;

        intentosHechos = 0;
        gano = false;
        termino = false;

        indiceSup = 0;
        indiceInf = 0;
        indiceSecreta = -1;

        limiteSup = null;
        limiteInf = null;

        letrasUsadas.clear();
        historial.clear();

        diccActual = this.idioma.equals("es") ? diccEs : diccEn;

        if (diccActual.isEmpty())
        {
            return false;
        }

        Collator col = Collator.getInstance(new Locale("es", "MX"));
        col.setStrength(Collator.SECONDARY);
        comparador = (a, b) -> col.compare(a, b);

        ordenadas = diccActual.keySet().stream()
            .filter(palabra -> palabra.length() == this.longitud)
            .sorted(comparador)
            .collect(Collectors.toCollection(ArrayList::new));

        if (ordenadas.isEmpty())
        {
            return false;
        }

        indiceSecreta = rnd.nextInt(ordenadas.size());
        palabraSecreta = ordenadas.get(indiceSecreta);

        indiceSup = 0;
        indiceInf = ordenadas.size() - 1;

        return true;
    }

    public boolean contienePalabra(String palabra)
    {
        if (diccActual == null)
        {
            return false;
        }
        return diccActual.containsKey(palabra.trim().toLowerCase(Locale.ROOT));
    }

    public String intentar(String palabra)
    {
        if (termino)
        {
            return "La partida ya terminó.";
        }

        palabra = palabra.trim().toLowerCase(Locale.ROOT);

        if (palabra.isEmpty())
        {
            return "Escribe una palabra válida.";
        }

        if (palabra.length() != longitud)
        {
            return "La palabra debe tener " + longitud + " letras.";
        }

        if (!contienePalabra(palabra))
        {
            return NO_EN_DICCIONARIO;
        }

        if (limiteSup != null)
        {
            if (comparador.compare(palabra, limiteSup) <= 0)
            {
                return "La palabra debe estar después de: " + limiteSup;
            }
        }

        if (limiteInf != null)
        {
            if (comparador.compare(palabra, limiteInf) >= 0)
            {
                return "La palabra debe estar antes de: " + limiteInf;
            }
        }

        historial.add("Intento " + intentosHechos + ": " + palabra);
        intentosHechos ++;

        for (char letra : palabra.toCharArray())
        {
            letrasUsadas.add(letra);
        }

        if (comparador.compare(palabra, palabraSecreta) == 0)
        {
            gano = true;
            termino = true;
            return "¡Correcto! Adivinaste la palabra secreta: " + palabraSecreta;
        }

        int indiceIntento = indiceDe(palabra);

        if(indiceIntento >= 0)
        {
            if(comparador.compare(palabra, palabraSecreta) < 0)
            {
                indiceSup = Math.max(indiceSup, indiceIntento + 1);
                limiteSup = palabra;
            }
            else
            {
                indiceInf = Math.min(indiceInf, indiceIntento - 1);
                limiteInf = palabra;
            }
        }

        String direccion = comparador.compare(palabra, palabraSecreta) < 0 ? "después" : "antes";
        String cercania = calcularCercania();

        if (intentosHechos >= intentosMax)
        {
            termino = true;
            return "La palabra secreta está " + direccion + " de tu intento. " +
                cercania + ".\nSe acabaron los intentos.";
        }

        return "La palabra secreta está " + direccion + " de tu intento. " +
            cercania + ".";
    }

    public String pedirPista(int tipo)
    {
        if (termino)
        {
            return "La partida ya terminó.";
        }

        int paso = Math.max(1, (int)Math.ceil(ordenadas.size() / 1));

        switch (tipo)
        {
            case 1:
            {
                if(limiteSup == null)
                {
                    return "No hay un límite superior establecido.";
                }

                int actual = indiceDe(limiteSup);
                int pos = Math.min(actual + paso, indiceSecreta - 1);

                limiteSup = ordenadas.get(pos);

                return "Límite superior movido a: " + limiteSup;
            }

            case 2:
            {
                if(limiteInf == null)
                {
                    return "No hay un límite inferior establecido.";
                }

                int actual = indiceDe(limiteInf);

                int pos = Math.max(actual - paso, indiceSecreta + 1);

                limiteInf = ordenadas.get(pos);

                return "Límite inferior movido a: " + limiteInf;
            }

            case 3:
            {
                return "Empieza con: " + palabraSecreta.charAt(0);
            }

            default:
            {
                return "Tipo inválido.";
            }
        }
    }

    public String agregarPalabra(String palabra)
    {
        palabra = palabra.trim().toLowerCase(Locale.ROOT);

        if (palabra.isEmpty())
        {
            return "No se puede agregar una palabra vacía.";
        }

        if (diccActual.containsKey(palabra))
        {
            return "Esa palabra ya existe en el diccionario.";
        }

        diccActual.put(palabra, palabra.length());

        if (idioma != null && idioma.startsWith("en"))
        {
            diccEn.put(palabra, palabra.length());
        }
        else
        {
            diccEs.put(palabra, palabra.length());
        }

        guardarEnArchivo(palabra);

        if (palabra.length() == longitud && comparador != null)
        {
            ordenadas.add(palabra);
            ordenadas.sort(comparador);
            indiceSecreta = ordenadas.indexOf(palabraSecreta);
            indiceSup = 0;
            indiceInf = ordenadas.size() - 1;
        }

        return "Palabra agregada al diccionario.";
    }

    private void guardarEnArchivo(String palabra)
    {
        try
        {
            String ruta = idioma.startsWith("es") ?
                "diccionario_es.txt" :
                "diccionario_en.txt";

            ArrayList<String> palabras = new ArrayList<>
            (
                Files.readAllLines(Paths.get(ruta))
            );

            palabra = palabra.trim().toLowerCase(Locale.ROOT);

            if(!palabras.contains(palabra))
            {
                palabras.add(palabra);
            }

            palabras.sort(comparador);
            Files.write(Paths.get(ruta), palabras, StandardCharsets.UTF_8);
        }
        catch(IOException e)
        {
            System.out.println("Error guardando archivo.");
        }
    }

    private int indiceDe(String palabra)
    {
        return Collections.binarySearch(ordenadas, palabra, comparador);
    }

    private String calcularCercania()
    {
        int indiceSup;
        int indiceInf;

        if (limiteSup == null)
        {
            indiceSup = 0;
        }
        else
        {
            indiceSup = indiceDe(limiteSup);
        }

        if (limiteInf == null)
        {
            indiceInf = ordenadas.size() - 1;
        }
        else
        {
            indiceInf = indiceDe(limiteInf);
        }

        int arriba = indiceSecreta - indiceSup;
        int abajo = indiceInf - indiceSecreta;

        if (arriba < abajo)
        {
            return "\nMás cerca del límite de arriba";
        }
        else if (arriba == abajo)
        {
            return "\nEn medio de los dos límites";
        }
        else
        {
            return "\nMás cerca del límite de abajo";
        }
    }

    private double porcentajePalabra(String palabra)
    {
        int indice = Collections.binarySearch(ordenadas, palabra, comparador);
        if (indice < 0)
        {
            indice = -indice - 1;
        }
        return (indice * 100.0) / ordenadas.size();
    }

    public double getPorcentajeArriba()
    {
        String arriba;

        if (limiteSup == null)
        {
            arriba = "a".repeat(longitud);
        }
        else
        {
            arriba = limiteSup;
        }

        double porcentajeSecreta = porcentajePalabra(palabraSecreta);
        double porcentajeArriba = porcentajePalabra(arriba);

        return porcentajeSecreta - porcentajeArriba;
    }

    public double getPorcentajeAbajo()
    {
        String abajo;

        if (limiteInf == null)
        {
            abajo = "z".repeat(longitud);
        }
        else
        {
            abajo = limiteInf;
        }

        double porcentajeSecreta = porcentajePalabra(palabraSecreta);
        double porcentajeAbajo = porcentajePalabra(abajo);

        return porcentajeAbajo - porcentajeSecreta;
    }

    public boolean termino()
    {
        return termino;
    }

    public boolean gano()
    {
        return gano;
    }

    public int getIntentosRestantes()
    {
        return Math.max(0, intentosMax - intentosHechos);
    }

    public int getIntentosHechos()
    {
        return intentosHechos;
    }

    public int getLongitud()
    {
        return longitud;
    }

    public String getIdioma()
    {
        return idioma;
    }

    public ArrayList<String> getHistorial()
    {
        return new ArrayList<>(historial);
    }

    public HashSet<Character> getLetrasUsadas()
    {
        return new HashSet<>(letrasUsadas);
    }

    public int getCantidadPalabrasRonda()
    {
        return ordenadas.size();
    }

    public String getLimiteSup()
    {
        return limiteSup;
    }

    public String getLimiteInf()
    {
        return limiteInf;
    }

    public String getPalabraSecreta()
    {
        return palabraSecreta;
    }
}