DROP TABLE IF EXISTS orchestration_process_step;
DROP TABLE IF EXISTS orchestration_process;

CREATE TABLE orchestration_process (
     id VARCHAR(16) NOT NULL PRIMARY KEY,
     status VARCHAR(50)
);

CREATE TABLE orchestration_process_step (
     id VARCHAR(16) NOT NULL PRIMARY KEY,
     orchestrator_process_id VARCHAR(16),
     step_type VARCHAR(50),
     name VARCHAR(100),
     error VARCHAR(10000),
     status_step VARCHAR(50),
     FOREIGN KEY (orchestrator_process_id) REFERENCES orchestration_process(id)
);