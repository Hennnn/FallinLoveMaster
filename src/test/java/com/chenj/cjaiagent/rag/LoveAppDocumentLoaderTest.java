package com.chenj.cjaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppDocumentLoaderTest {
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Test
    void loadMarkdowns() {
        loveAppDocumentLoader.loadMarkdowns();
    }
}