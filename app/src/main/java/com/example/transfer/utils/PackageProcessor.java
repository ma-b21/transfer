package com.example.transfer.utils;


public class PackageProcessor {
    private PackageProcessor() {
        throw new UnsupportedOperationException("此类不能被实例化");
    }

    public static PackageModel[] generatePackages(String input) {
        byte[] data = StringProcessor.encode_string_to_bytes(input);
        int packageCount = (int) Math.ceil(data.length / 10.0);
        PackageModel[] packages = new PackageModel[packageCount];
        for (int i = 0; i < packageCount; i++) {
            int packageLength = Math.min(10, data.length - i * 10);
            byte[] packageData = new byte[packageLength + 1];
            System.arraycopy(data, i * 10, packageData, 0, packageLength);
            packageData[packageLength] = StringProcessor.encode_string_to_bytes(" ")[0];
            if (i == packageCount - 1) {
                packages[i] = new PackageModel(packageData, PackageModel.PackageType.END, (byte) i);
            } else {
                packages[i] = new PackageModel(packageData, PackageModel.PackageType.DATA, (byte) i);
            }
        }
        return packages;
    }
}
