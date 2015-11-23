package br.gov.servicos.orgao;

import br.gov.servicos.servico.ServicoRepository;
import br.gov.servicos.v3.schema.OrgaoXML;
import br.gov.servicos.v3.schema.ServicoXML;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrgaoRepositoryUtil {
    ServicoRepository servicos;
    OrgaoRepository orgaoRepository;

    @Autowired
    OrgaoRepositoryUtil(ServicoRepository servicos, OrgaoRepository orgaoRepository) {
        this.servicos = servicos;
        this.orgaoRepository = orgaoRepository;
    }

    @Cacheable("orgaos")
    public SortedSet<OrgaoXML> findAll() {
        return servicos.findAll(new PageRequest(0, Integer.MAX_VALUE)).getContent()
                .stream()
                .filter(s -> Objects.nonNull(s.getOrgao()))
                .map(ServicoXML::getOrgao)
                .collect(toCollection(() -> new TreeSet<>(comparing(OrgaoXML::getNome))));
    }

}
