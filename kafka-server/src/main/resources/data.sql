DROP TABLE IF EXISTS orchestrator_process_step;
DROP TABLE IF EXISTS orchestrator_process;

CREATE TABLE orchestrator_process (
     id BINARY(16) NOT NULL PRIMARY KEY,
     status VARCHAR(50)
);

CREATE TABLE orchestrator_process_step (
     id BINARY(16) NOT NULL PRIMARY KEY,
     orchestrator_process_id BINARY(16),
     step_type VARCHAR(50),
     name VARCHAR(100),
     error VARCHAR(10000),
     status_step VARCHAR(50),
     FOREIGN KEY (orchestrator_process_id) REFERENCES orchestrator_process(id)
);