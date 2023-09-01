package chatdocs;


import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.ParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import static dev.langchain4j.data.document.FileSystemDocumentLoader.loadDocument;
import static java.time.Duration.ofSeconds;




public class Test {
	
	//HuggingFace API
    public static final String HF_API_KEY = "token";
	
	public static void main(String[] args) throws Exception {

        Document document = loadDocument(toPath("text.txt"));
        
        //choosing an embedding model
        EmbeddingModel embeddingModel = HuggingFaceEmbeddingModel.builder()
                .accessToken(HF_API_KEY)
                .modelId("sentence-transformers/all-MiniLM-L6-v2")
                .waitForModel(true)
                .timeout(ofSeconds(60))
                .build();
        
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        //embedding doc
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .splitter(new ParagraphSplitter())
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);

        
        
        
 
        //choosing a model to predict
        ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder()
                .chatLanguageModel(HuggingFaceChatModel.withAccessToken(HF_API_KEY))
                .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
                // .chatMemory() // you can override default chat memory
                // .promptTemplate() // you can override default prompt template
                .build();
        
        
        //predict
        String answer = chain.execute("Where does Charlie lives?");
        System.out.println(answer);
        
	}
	
	private static Path toPath(String fileName) {
        try {
            URL fileUrl = Test.class.getResource(fileName);
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
