import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Класс для определения пути к каталогу, из которого запущен jar-файл.
 *
 */
public class ApplicationStartUpPath {

    /**
     * @return Путь к каталогу, в котором расположен jar-файл с классом
     *         ApplicationStartUpPath.
     */
    public Path getApplicationStartUp() throws UnsupportedEncodingException,
            MalformedURLException {
        URL startupUrl = getClass().getProtectionDomain().getCodeSource()
                .getLocation();
        Path path = null;
        try {
            path = Paths.get(startupUrl.toURI());
        } catch (Exception e) {
            try {
                path = Paths.get(new URL(startupUrl.getPath()).getPath());
            } catch (Exception ipe) {
                path = Paths.get(startupUrl.getPath());
            }
        }
        path = path.getParent();
        return path;
    }
}