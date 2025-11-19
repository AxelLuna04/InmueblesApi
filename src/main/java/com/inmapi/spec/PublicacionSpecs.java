package com.inmapi.spec;

import com.inmapi.modelo.Publicacion;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class PublicacionSpecs {

    public static Specification<Publicacion> operacion(String op) {
        return (op == null || op.isBlank()) ? null : (r, q, cb) -> cb.equal(r.get("tipoOperacion"), op);
    }

    public static Specification<Publicacion> estadoAprobada() {
        return (r, q, cb) -> cb.equal(r.get("estado"), "APROBADA");
    }
    
    public static Specification<Publicacion> porEstado(String estado) {
        if (estado == null || estado.isBlank()) return null;
        return (r, q, cb) -> cb.equal(r.get("estado"), estado);
      }

    public static Specification<Publicacion> tipo(Integer idTipo) {
        return idTipo == null ? null : (r, q, cb) -> cb.equal(r.get("tipoInmueble").get("id"), idTipo);
    }

    public static Specification<Publicacion> precioMin(Double v) {
        return v == null ? null : (r, q, cb) -> cb.greaterThanOrEqualTo(r.get("precio"), v);
    }

    public static Specification<Publicacion> precioMax(Double v) {
        return v == null ? null : (r, q, cb) -> cb.lessThanOrEqualTo(r.get("precio"), v);
    }

    public static Specification<Publicacion> habMin(Integer v) {
        return v == null ? null : (r, q, cb) -> cb.greaterThanOrEqualTo(r.get("numeroHabitaciones"), v);
    }

    public static Specification<Publicacion> banosMin(Integer v) {
        return v == null ? null : (r, q, cb) -> cb.greaterThanOrEqualTo(r.get("numeroBanosCompletos"), v);
    }

    public static Specification<Publicacion> excusadosMin(Integer v) {
        return v == null ? null : (r, q, cb) -> cb.greaterThanOrEqualTo(r.get("numeroExcusados"), v);
    }

    public static Specification<Publicacion> texto(String qText) {
        if (qText == null || qText.isBlank()) {
            return null;
        }
        return (r, q, cb) -> {
            String like = "%" + qText.trim().toLowerCase() + "%";
            var titulo = cb.like(cb.lower(r.get("titulo")), like);
            var desc = cb.like(cb.lower(r.get("descripcion")), like);
            // include address
            var dir = r.join("direccion", JoinType.LEFT);
            var addr = cb.like(cb.lower(dir.get("formattedAddress")), like);
            return cb.or(titulo, desc, addr);
        };
    }

    public static Specification<Publicacion> ubicacion(String u) {
        if (u == null || u.isBlank()) {
            return null;
        }
        return (r, q, cb) -> {
            String like = "%" + u.trim().toLowerCase() + "%";
            var d = r.join("direccion", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(d.get("locality")), like),
                    cb.like(cb.lower(d.get("sublocality")), like),
                    cb.like(cb.lower(d.get("formattedAddress")), like)
            );
        };
    }

    // Publicaciones que contienen TODAS las características requeridas (ids)
    public static Specification<Publicacion> conTodasCaracts(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> {
            // count distinct matches == ids.size
            var join = root.join("caracteristicas", JoinType.LEFT).join("caracteristica", JoinType.LEFT);
            query.groupBy(root.get("id"));
            return cb.equal(cb.countDistinct(join.get("id")), ids.size());
        };
    }
}

