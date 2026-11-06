import com.android.apksig.ApkVerifier;
import java.io.File;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.HexFormat;
import java.util.List;

public final class VerifyApkSignature {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("usage: VerifyApkSignature <apk>");
        }
        ApkVerifier.Result result = new ApkVerifier.Builder(new File(args[0])).build().verify();
        System.out.println("verified=" + result.isVerified());
        System.out.println("v1=" + result.isVerifiedUsingV1Scheme());
        System.out.println("v2=" + result.isVerifiedUsingV2Scheme());
        System.out.println("v3=" + result.isVerifiedUsingV3Scheme());
        System.out.println("v31=" + result.isVerifiedUsingV31Scheme());
        System.out.println("v4=" + result.isVerifiedUsingV4Scheme());
        List<X509Certificate> certificates = result.getSignerCertificates();
        for (X509Certificate certificate : certificates) {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(certificate.getEncoded());
            System.out.println("certificate-sha256=" + HexFormat.of().formatHex(digest));
            System.out.println("subject=" + certificate.toString().split("\\n")[0]);
        }
        if (result.containsErrors()) {
            result.getAllErrors().forEach(error -> System.err.println("ERROR: " + error));
            System.exit(1);
        }
        result.getWarnings().forEach(warning -> System.out.println("WARNING: " + warning));
    }
}
