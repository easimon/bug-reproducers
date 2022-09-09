import java.io.IOException;
import java.nio.file.*;

public class Main {

    public static void main(String[] args) throws Exception {
        println("Getting files in jurisdiction policy folder");
        println(getJurisdictionPolicies());
    }

    /**
     * Extraction of relevant parts of {@link javax.crypto.JceSecurity#setupJurisdictionPolicies()}
     * that trigger the bug. Starting at Line 331, search for:
     *
     * <code>
     * try (DirectoryStream<Path> stream = Files.newDirectoryStream(...
     * </code>
     */
    private static String getJurisdictionPolicies() throws IOException {
        String javaHome = System.getProperty("java.home");
        String cryptoPolicy = "unlimited";
        Path path = Paths.get(javaHome, "conf", "security", "policy", cryptoPolicy).normalize();

        StringBuilder builder = new StringBuilder();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*")) {
            for (Path entry : stream) {
                String filename = entry.getFileName().toString();
                builder.append("  ").append(filename).append("\n");
            }
        } catch (DirectoryIteratorException ex) {
            // JceSecurity throws a SecurityException here, swallowing the original DirectoryIteratorException.
            // throw new SecurityException("Couldn't iterate through the jurisdiction policy files: " + cryptoPolicyProperty);
            throw ex;
        }
        return builder.toString();
    }

    private static void println(String line) {
        System.out.println(line);
    }
}
