package cl.duoc.bancoxyz.bff.mobile.controller;

import cl.duoc.bancoxyz.bff.mobile.dto.MobileResumenCuentaResponse;
import cl.duoc.bancoxyz.bff.mobile.service.BffMobileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bff/mobile")
public class BffMobileController {

    private final BffMobileService mobileService;

    public BffMobileController(BffMobileService mobileService) {
        this.mobileService = mobileService;
    }

    @GetMapping("/cuentas/{cuentaId}/resumen")
    public ResponseEntity<MobileResumenCuentaResponse> obtenerResumen(@PathVariable Long cuentaId) {
        return ResponseEntity.ok(mobileService.obtenerResumen(cuentaId));
    }
}