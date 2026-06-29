package com.carlosribeiro.apirestful.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Estratégia de migração do Flyway: roda {@code repair()} antes de {@code migrate()}.
 *
 * O objetivo é garantir que QUALQUER pessoa chegue ao mesmo resultado correto ao
 * subir o Docker (mesmos produtos), sem precisar apagar volume na mão — tanto quem
 * nunca rodou o projeto quanto quem já tinha o banco antigo. Os três cenários:
 *
 * <ul>
 *   <li><b>Base nova:</b> {@code repair()} é no-op; {@code migrate()} aplica V1..V6.</li>
 *   <li><b>Já aplicou a V6 antiga com sucesso:</b> como a V6 foi editada (para não
 *       quebrar por foreign key), o checksum mudou. {@code repair()} realinha o
 *       checksum no histórico e {@code migrate()} não re-executa nada — o catálogo
 *       de produtos já é o correto, pois o INSERT dos 45 produtos não mudou.</li>
 *   <li><b>V6 antiga falhou (erro de FK):</b> {@code repair()} remove a entrada
 *       falhada do histórico e {@code migrate()} re-executa a V6 corrigida, que
 *       agora limpa as referências antes de trocar os produtos.</li>
 * </ul>
 *
 * Observação: rodar {@code repair()} a cada boot também "perdoa" edições acidentais
 * em migrações já aplicadas — aceitável neste projeto, em que o banco é todo
 * seed/demo e reproduzível a partir das migrações.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy repairBeforeMigrate() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
