package com.sismics.docs.core.service;

import com.sismics.docs.BaseTransactionalTest;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.EncryptionUtil;
import com.sismics.docs.core.util.FileUtil;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Additional tests to raise instruction and branch coverage (Practice 8).
 */
public class Practice8CoverageTest extends BaseTransactionalTest {

    @Test
    public void encryptionUtilDecryptFile_nullPrivateKeyReturnsOriginalPath() throws Exception {
        Path tmp = Files.createTempFile("practice8_decrypt", ".bin");
        try {
            Path result = EncryptionUtil.decryptFile(tmp, null);
            Assert.assertSame(tmp, result);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    public void fileUtilDelete_whenNoSidecarFiles_existOnlyMainIsRemoved() throws Exception {
        String id = "practice8_partial_" + System.nanoTime();
        Path stored = DirectoryUtil.getStorageDirectory().resolve(id);
        Files.write(stored, new byte[]{1});
        FileUtil.delete(id);
        Assert.assertFalse(Files.exists(stored));
    }

    @Test
    public void fileUtilDelete_whenAllVariantsExist_removesAll() throws Exception {
        String id = "practice8_full_" + System.nanoTime();
        Path base = DirectoryUtil.getStorageDirectory();
        Path stored = base.resolve(id);
        Path web = base.resolve(id + "_web");
        Path thumb = base.resolve(id + "_thumb");
        Files.write(stored, new byte[]{1});
        Files.write(web, new byte[]{2});
        Files.write(thumb, new byte[]{3});  
        FileUtil.delete(id);
        Assert.assertFalse(Files.exists(stored));
        Assert.assertFalse(Files.exists(web));
        Assert.assertFalse(Files.exists(thumb));
    }

    @Test
    public void fileUtilDelete_whenNothingExists_completesQuietly() throws Exception {
        FileUtil.delete("practice8_missing_" + System.nanoTime());
    }

    @Test
    public void fileSizeServiceProcessFile_whenUserMissing_returnsWithoutUpdate() {
        FileSizeService service = new FileSizeService();
        File file = new File();
        file.setId("any-id");
        file.setUserId("00000000-0000-0000-0000-000000000001");
        service.processFile(file);
    }

    @Test
    public void fileSizeServiceProcessFile_whenPhysicalFileMissing_skipsPersist() throws Exception {
        User user = createUser("practice8FileSize");
        FileSizeService service = new FileSizeService();
        File file = new File();
        file.setId("missing_on_disk_" + System.nanoTime());
        file.setUserId(user.getId());
        service.processFile(file);
    }
}
