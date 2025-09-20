package domina.springboot.verduleria.back;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VerduleriaRestTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void consultarTomateTest() {
        ResponseEntity<String> response = restTemplate.getForEntity("/verduras/2", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(2);

        String nombre = documentContext.read("$.nombre");
        assertThat(nombre).isEqualTo("Tomate");

        Double precio = documentContext.read("$.precio");
        assertThat(precio).isEqualTo(10);

        Boolean troceable = documentContext.read("$.troceable");
        assertThat(troceable).isEqualTo(false);

    }

    @Test
    void crearRemolachaTest() {
        Verdura remolacha = new Verdura(0, "Remolacha", 4.52, false);
        ResponseEntity<Verdura> createResponse = restTemplate.postForEntity("/verduras", remolacha, Verdura.class);

        // Comprobacion status de respuesta
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Recuperacion URI del objeto creado
        URI remolachaLocation = createResponse.getHeaders().getLocation();
        // Recuperacion nuevo id
        Number nuevoId = recuperarId(remolachaLocation);

        // Comprobación body respuesta
        Verdura remolachaCreada = JsonPath.parse(createResponse.getBody()).json();
        assertThat(remolachaCreada.getId()).isEqualTo(nuevoId);
        assertThat(remolachaCreada.getNombre()).isEqualTo("Remolacha");
        assertThat(remolachaCreada.getPrecio()).isEqualTo(4.52);
        assertThat(remolachaCreada.isTroceable()).isFalse();

        // Comprobacion get by id del objeto creado
        ResponseEntity<String> getResponse = restTemplate.getForEntity(remolachaLocation, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Number id = documentContext.read("$.id");
        assertThat(id).isNotNull();
        assertThat(id).isNotEqualTo(0);
        assertThat(Long.valueOf(id.longValue())).isEqualTo((long)nuevoId);

        String nombre = documentContext.read(("$.nombre"));
        assertThat(nombre).isEqualTo("Remolacha");

        Double precio = documentContext.read("$.precio");
        assertThat(precio).isEqualTo(4.52);

        Boolean troceable = documentContext.read("$.troceable");
        assertThat(troceable).isFalse();

    }

    private long recuperarId(URI remolachaLocation) {
        String path = remolachaLocation.getPath();
        String idText = path.substring(path.lastIndexOf("/")+1);
        return Long.parseLong(idText);
    }
}
