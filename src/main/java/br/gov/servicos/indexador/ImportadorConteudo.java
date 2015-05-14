package br.gov.servicos.indexador;

import br.gov.servicos.cms.Conteudo;
import br.gov.servicos.cms.ConteudoRepository;
import br.gov.servicos.foundation.exceptions.ConteudoNaoEncontrado;
import br.gov.servicos.orgao.OrgaoRepository;
import br.gov.servicos.servico.Orgao;
import br.gov.servicos.servico.linhaDaVida.LinhaDaVida;
import br.gov.servicos.servico.linhaDaVida.LinhaDaVidaRepository;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Component
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ImportadorConteudo {

    LinhaDaVidaRepository linhaDaVidaRepository;
    OrgaoRepository orgaoRepository;
    ConteudoRepository conteudoRepository;

    @Autowired
    public ImportadorConteudo(LinhaDaVidaRepository linhaDaVidaRepository, OrgaoRepository orgaoRepository, ConteudoRepository conteudoRepository) {
        this.linhaDaVidaRepository = linhaDaVidaRepository;
        this.orgaoRepository = orgaoRepository;
        this.conteudoRepository = conteudoRepository;
    }

    public Iterable<Conteudo> importar() {
        List<Conteudo> conteudos = concat(
                orgaoRepository.findAll().stream().map(this::paraConteudo),
                linhaDaVidaRepository.findAll().stream().map(this::paraConteudo)
        ).collect(toList());

        return this.conteudoRepository.save(conteudos);
    }

    private Conteudo paraConteudo(LinhaDaVida linhaDaVida) {
        return new Conteudo()
                .withId(linhaDaVida.getId())
                .withTipoConteudo("linha-da-vida")
                .withTitulo(linhaDaVida.getTitulo())
                .withConteudo(conteudo(format("/conteudo/linhas-da-vida/%s.md", linhaDaVida.getId())));
    }

    private Conteudo paraConteudo(Orgao orgao) {
        return new Conteudo()
                .withId(orgao.getId())
                .withTipoConteudo("orgao")
                .withTitulo(orgao.getNome())
                .withConteudo(conteudo(format("/conteudo/orgaos/%s.md", orgao.getId())));
    }

    private String conteudo(String conteudoId) {
        InputStreamReader input = null;
        try {
            URL resource = new ClassPathResource(conteudoId).getURL();
            log.debug("Conteúdo {} encontrado em: {}", conteudoId, resource);
            input = new InputStreamReader(resource.openStream(), "UTF-8");
            try (BufferedReader br = new BufferedReader(input)) {
                return br.lines().collect(joining("\n"));
            }
        } catch (IOException e) {
            throw new ConteudoNaoEncontrado(e);
        }
    }

}
