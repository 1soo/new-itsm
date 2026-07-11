package com.itsm.knowledge.application;

import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.UserStatus;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.approval.application.TicketSummary;
import com.itsm.knowledge.domain.KnowledgeArticle;
import com.itsm.knowledge.domain.repository.KnowledgeArticleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeApprovalTicketSummaryProviderTest {

    @Mock
    private KnowledgeArticleRepository articleRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Test
    void summaryOfSynthesizesTicketKeyFromArticleId() throws Exception {
        KnowledgeApprovalTicketSummaryProvider provider =
                new KnowledgeApprovalTicketSummaryProvider(articleRepository, appUserRepository);
        KnowledgeArticle article = new KnowledgeArticle("제목", "본문", null, 10L);
        setId(article, 42L);
        AppUser author = new AppUser("author@itsm.local", "encoded", "작성자", UserStatus.ACTIVE);

        when(articleRepository.findById(42L)).thenReturn(java.util.Optional.of(article));
        when(appUserRepository.findById(10L)).thenReturn(java.util.Optional.of(author));

        TicketSummary summary = provider.summaryOf(42L);

        assertThat(summary.ticketKey()).isEqualTo("KM-42");
        assertThat(summary.title()).isEqualTo("제목");
        assertThat(summary.requesterName()).isEqualTo("작성자");
    }

    private void setId(KnowledgeArticle article, Long id) throws Exception {
        Field field = article.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(article, id);
    }
}
