INSERT INTO polizas (tipo, estado, vigencia_meses, valor_canon, valor_prima, fecha_inicio, fecha_fin, created_at, updated_at) 
VALUES ('INDIVIDUAL', 'ACTIVA', 12, 1000.00, 12000.00, '2026-01-01', '2026-12-31', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO polizas (tipo, estado, vigencia_meses, valor_canon, valor_prima, fecha_inicio, fecha_fin, created_at, updated_at) 
VALUES ('COLECTIVA', 'ACTIVA', 24, 5000.00, 120000.00, '2026-06-01', '2028-05-31', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO polizas (tipo, estado, vigencia_meses, valor_canon, valor_prima, fecha_inicio, fecha_fin, created_at, updated_at) 
VALUES ('INDIVIDUAL', 'CANCELADA', 12, 800.00, 9600.00, '2025-01-01', '2025-12-31', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO riesgos (descripcion, estado, poliza_id, created_at, updated_at) 
VALUES ('Riesgo de Impago Arrendatario 1', 'ACTIVO', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO riesgos (descripcion, estado, poliza_id, created_at, updated_at) 
VALUES ('Riesgo Daños a Propiedad', 'ACTIVO', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO riesgos (descripcion, estado, poliza_id, created_at, updated_at) 
VALUES ('Riesgo Responsabilidad Civil', 'ACTIVO', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO riesgos (descripcion, estado, poliza_id, created_at, updated_at) 
VALUES ('Riesgo Histórico Cancelado', 'CANCELADO', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
