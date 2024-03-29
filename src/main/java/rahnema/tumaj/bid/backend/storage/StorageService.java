package rahnema.tumaj.bid.backend.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

//    void init();
    String store(MultipartFile file,String storage);

//    Stream<Path> loadAll();

    Path load(String filename,String storage);

    Resource loadAsResource(String filename,String storage);


//    void deleteAll();

}
