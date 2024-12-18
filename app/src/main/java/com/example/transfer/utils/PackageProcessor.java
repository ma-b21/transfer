package com.example.transfer.utils;


public class PackageProcessor {
    private PackageProcessor() {
        throw new UnsupportedOperationException("此类不能被实例化");
    }

    public static PackageModel[] generatePackages(String input) {
        byte[] data = StringProcessor.encode_string_to_bytes(input);
        int packageCount = (int) Math.ceil(data.length / 20.0);
        PackageModel[] packages = new PackageModel[packageCount];
        for (int i = 0; i < packageCount; i++) {
            int packageLength = Math.min(20, data.length - i * 20);
            byte[] packageData = new byte[packageLength];
            System.arraycopy(data, i * 20, packageData, 0, packageLength);
            if (i == packageCount - 1) {
                packages[i] = new PackageModel(packageData, PackageModel.PackageType.END, (byte) i);
            } else {
                packages[i] = new PackageModel(packageData, PackageModel.PackageType.DATA, (byte) i);
            }
        }
        return packages;
    }

    public static String extractPackages(PackageModel[] packages) {
        StringBuilder result = new StringBuilder();
        for (PackageModel p : packages) {
            result.append(StringProcessor.decode_bytes_to_string(p.getData()));
        }
        return result.toString();
    }
}
