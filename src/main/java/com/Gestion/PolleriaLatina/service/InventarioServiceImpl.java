package com.Gestion.PolleriaLatina.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.model.Insumo;
import com.Gestion.PolleriaLatina.model.InventarioCabecera;
import com.Gestion.PolleriaLatina.model.InventarioDetalle;
import com.Gestion.PolleriaLatina.model.KardexMovimiento;
import com.Gestion.PolleriaLatina.model.Usuario;
import com.Gestion.PolleriaLatina.model.enumerados.TipoMovimiento;
import com.Gestion.PolleriaLatina.repository.InsumoRepository;
import com.Gestion.PolleriaLatina.repository.InventarioCabeceraRepository;
import com.Gestion.PolleriaLatina.repository.KardexMovimientoRepository;
import com.Gestion.PolleriaLatina.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

  private final InventarioCabeceraRepository cabeceraRepository;
  private final InsumoRepository insumoRepository;
  private final KardexMovimientoRepository kardexRepository;
  private final UsuarioRepository usuarioRepository;

  @Override
  @Transactional(readOnly = true)
  public List<InventarioCabecera> listarHistorial() {
    return cabeceraRepository.findAllByOrderByFechaInventarioDesc();
  }

  @Override
  @Transactional(readOnly = true)
  public InventarioCabecera obtenerPorId(Long id) {
    return cabeceraRepository.findById(id).orElse(null);
  }

  @Override
  @Transactional
  public void procesarTomaInventario(InventarioCabecera inventario) {
    // Mantener firma base limpia por jerarquía estructural
    cabeceraRepository.save(inventario);
  }

  @Override
  @Transactional
  public void procesarAjusteMasivo(String tipoMovimiento, String motivo, String referencia,
      List<Long> insumoIds, List<Double> cantidades, String username) {

    Usuario usuario = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Usuario en sesión no válido."));

    // 1. Crear la cabecera de la transacción de almacén
    InventarioCabecera cabecera = InventarioCabecera.builder()
        .usuario(usuario)
        .observaciones("TIPO: " + tipoMovimiento + " | MOTIVO: " + motivo + " | REF: "
            + (referencia.isBlank() ? "S/R" : referencia))
        .detalles(new ArrayList<>())
        .build();

    InventarioCabecera cabeceraGuardada = cabeceraRepository.save(cabecera);

    // 2. Procesar fila por fila los insumos agregados
    for (int i = 0; i < insumoIds.size(); i++) {
      Long insumoId = insumoIds.get(i);
      Double cantidadAjuste = cantidades.get(i);

      Insumo insumo = insumoRepository.findById(insumoId)
          .orElseThrow(() -> new RuntimeException("Insumo con ID " + insumoId + " no existe."));

      double stockTeoricoAntes = insumo.getStockActual();
      double stockFisicoResultante;

      // Evaluamos si suma o resta al almacén central
      if ("ENTRADA".equalsIgnoreCase(tipoMovimiento)) {
        stockFisicoResultante = stockTeoricoAntes + cantidadAjuste;
      } else {
        stockFisicoResultante = stockTeoricoAntes - cantidadAjuste;
        if (stockFisicoResultante < 0) {
          throw new RuntimeException("Operación inválida: El insumo '" + insumo.getNombre()
              + "' no puede quedar con stock negativo (" + stockFisicoResultante + ").");
        }
      }

      // 3. Registrar el detalle del reporte histórico
      InventarioDetalle detalle = InventarioDetalle.builder()
          .inventarioCabecera(cabeceraGuardada)
          .insumo(insumo)
          .stockTeorico(stockTeoricoAntes)
          .stockFisico(stockFisicoResultante)
          .diferencia("ENTRADA".equalsIgnoreCase(tipoMovimiento) ? cantidadAjuste : -cantidadAjuste)
          .build();
      cabeceraGuardada.getDetalles().add(detalle);

      // 4. Modificar el stock real de la materia prima
      insumo.setStockActual(stockFisicoResultante);
      insumoRepository.save(insumo);

      // 5. REGISTRAR EN KARDEX EL MOVIMIENTO INMUTABLE
      KardexMovimiento movimientoKardex = KardexMovimiento.builder()
          .insumo(insumo)
          .tipoMovimiento("ENTRADA".equalsIgnoreCase(tipoMovimiento) ? TipoMovimiento.ENTRADA : TipoMovimiento.SALIDA)
          .cantidad(cantidadAjuste)
          .stockResultante(stockFisicoResultante)
          .origen("AJUSTE MASIVO #" + cabeceraGuardada.getId())
          .observacion("Motivo del ajuste: " + motivo + ". Referencia: " + referencia)
          .usuario(usuario)
          .build();
      kardexRepository.save(movimientoKardex);
    }

    cabeceraRepository.save(cabeceraGuardada);
  }
}