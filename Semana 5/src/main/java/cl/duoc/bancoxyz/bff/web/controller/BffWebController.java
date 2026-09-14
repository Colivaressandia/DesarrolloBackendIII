package cl.duoc.bancoxyz.bff.web.controller;

import cl.duoc.bancoxyz.bff.web.dto.WebPosicionGlobalResponse;
import cl.duoc.bancoxyz.bff.web.service.BffWebService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bff/web")
public class BffWebController {

    private final BffWebService webService;

    public BffWebController(BffWebService webService) {
        this.webService = webService;
    }

    @GetMapping("/posicion-global")
    public ResponseEntity<WebPosicionGlobalResponse> obtenerPosicionGlobal(@RequestParam("cliente") String nombre) {
        return ResponseEntity.ok(webService.obtenerPosicionGlobal(nombre));
    }
}