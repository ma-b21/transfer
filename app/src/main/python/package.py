class Package:
    def __init__(self, preamble, length, type, number, checksum, data):
        self.preamble = preamble
        self.length = length
        self.type = type
        self.number = number
        self.checksum = checksum
        self.data = data
    
    def __init__(self, binary):
        if len(binary) < 56:
            raise ValueError("Package length is less than 48.")
        self.preamble = binary[:24]
        self.length = binary[24:32]
        self.type = binary[32:40]
        self.number = binary[40:48]
        self.checksum = binary[48:56]
        self.data = binary[56:]

    def get_length(self):
        return int(self.length, 2)