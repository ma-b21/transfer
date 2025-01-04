import numpy as np
from scipy.signal import butter, filtfilt
from package import *


def qpsk_modulate(code, symbol_duration=0.025, sampling_rate=48000, signal_freq=20000, amplitude=1):
    # 检测输入数据是否为偶数
    if len(code) % 2 != 0:
        raise ValueError("输入数据长度必须为偶数")

    # 将输入的二进制数据拆分为两个比特一组
    bits = [code[i:i + 2] for i in range(0, len(code), 2)]

    # 映射至星座图
    symbol_map = {
        '00': (-np.sqrt(2) / 2, -np.sqrt(2) / 2),
        '01': (-np.sqrt(2) / 2, np.sqrt(2) / 2),
        '10': (np.sqrt(2) / 2, -np.sqrt(2) / 2),
        '11': (np.sqrt(2) / 2, np.sqrt(2) / 2)
    }

    I = []  # I路
    Q = []  # Q路

    for bit in bits:
        i_val, q_val = symbol_map[bit]
        I.append(i_val)
        Q.append(q_val)

    # 生成信号
    signal = np.array([])
    t = np.linspace(0, symbol_duration * len(I),
                    int(sampling_rate * symbol_duration) * len(I), endpoint=False)
    for i in range(len(I)):
        # 生成调制信号
        t1 = t[i * int(sampling_rate * symbol_duration): (i + 1)
               * int(sampling_rate * symbol_duration)]
        modulated_signal = amplitude * (I[i] * np.cos(2 * np.pi * signal_freq * t1) +
                                        Q[i] * np.sin(2 * np.pi * signal_freq * t1))
        signal = np.concatenate((signal, modulated_signal))

    # 将信号缩放至[-32768, 32767]范围，并转换为整数
    signal = np.int16(signal / np.max(np.abs(signal)) * 32767.0)

    return signal


def bandpass_filter(data, lowcut, highcut, fs, order=4):
    """  
    使用Butterworth带通滤波器对数据进行滤波  

    参数:  
        data (np.ndarray): 输入数据  
        lowcut (float): 滤波下限频率（Hz）  
        highcut (float): 滤波上限频率（Hz）  
        fs (float): 采样率（Hz）  
        order (int): 滤波器阶数  
    返回:  
        filtered_data (np.ndarray): 滤波后的数据  
    """
    nyquist = 0.5 * fs
    low = lowcut / nyquist
    high = highcut / nyquist
    b, a = butter(order, [low, high], btype='bandpass')
    filtered_data = filtfilt(b, a, data)
    return filtered_data


def generate_reference_wave(preamble_bits, symbol_duration, sampling_rate, signal_freq):
    """  
    根据前导码bit串(如'1110001001011100')，  
    生成对应的时域参考波形，用于相关检测。  
    """
    # 每个符号2比特，因此一次取2比特 -> 1个符号
    symbol_samples = int(sampling_rate * symbol_duration)
    ref_wave = []

    # 按2比特一组切分preamble_bits
    for i in range(0, len(preamble_bits), 2):
        bit_pair = preamble_bits[i:i+2]
        # 映射到星座图
        symbol_map = {
            '00': (-np.sqrt(2) / 2, -np.sqrt(2) / 2),
            '01': (-np.sqrt(2) / 2, np.sqrt(2) / 2),
            '10': (np.sqrt(2) / 2, -np.sqrt(2) / 2),
            '11': (np.sqrt(2) / 2, np.sqrt(2) / 2)
        }
        I, Q = symbol_map[bit_pair]

        # 生成符号波形
        t = np.linspace(0, symbol_duration, symbol_samples, endpoint=False)
        symbol_wave = I * np.cos(2 * np.pi * signal_freq * t) + \
            Q * np.sin(2 * np.pi * signal_freq * t)
        ref_wave.append(symbol_wave)

    # 将所有符号波形拼接
    return np.concatenate(ref_wave)


def qpsk_demodulate(signal, symbol_duration=0.025, sampling_rate=48000, signal_freq=20000, bits=None):
    # signal = np.array(signal, dtype=np.float64) / 32767.0

    # 使用带通滤波器滤波
    # signal = bandpass_filter(signal, signal_freq - 1000, signal_freq + 1000, sampling_rate)

    # 计算符号数量
    symbol_samples = int(sampling_rate * symbol_duration)
    if bits is None:
        num_symbols = len(signal) // symbol_samples
    else:
        num_symbols = bits // 2
        if num_symbols * symbol_samples > len(signal):
            raise ValueError("输入数据长度不足以解调")

    # 初始化I和Q值
    I = np.zeros(num_symbols)
    Q = np.zeros(num_symbols)

    # 解调过程
    for i in range(num_symbols):
        # 获取每个符号的样本
        sample = signal[i * symbol_samples: (i + 1) * symbol_samples]
        # 通过与载波相乘来提取I和Q分量
        t = np.linspace(0, symbol_duration, symbol_samples, endpoint=False)
        I[i] = np.sum(sample * np.cos(2 * np.pi * signal_freq * t))
        Q[i] = np.sum(sample * np.sin(2 * np.pi * signal_freq * t))

    # 确定星座图映射
    decoded_bits = []
    for i in range(num_symbols):
        if I[i] < 0 and Q[i] < 0:
            decoded_bits.append('00')
        elif I[i] < 0 and Q[i] >= 0:
            decoded_bits.append('01')
        elif I[i] >= 0 and Q[i] < 0:
            decoded_bits.append('10')
        else:
            decoded_bits.append('11')

    # 将比特组合成最终的二进制数据
    decoded_data = ''.join(decoded_bits)
    return decoded_data


def handle_receive(signal, symbol_duration, sampling_rate, signal_freq):
    """  
    在原先示例基础上，修正前导码采样点长度，确保使用完整16位前导码。  
    示例流程:  
      1) 带通滤波  
      2) 生成并匹配 16 位前导码  
      3) 估计并补偿 CFO  
      4) 估计并补偿 CPO  
      5) 解调剩余数据  
    注意：真实系统中，如果 signal 是实数带通，需要先下变频到复数基带，才能执行CFO/CPO估计。  
    """
    # ========= 2. 生成本地参考前导码 =========
    # 假设 ref_bits 即 24 位前导码
    ref_bits = '111000100101111100110101'   # length = 24
    ref_wave = generate_reference_wave(
        ref_bits, symbol_duration, sampling_rate, signal_freq)

    # 计算与参考前导码的相关，寻找峰值
    corr = np.correlate(signal, ref_wave, mode='valid')
    idx = np.argmax(corr)
    if max(corr) < 1:
        return "未检测到前导码"

    # 反复搜索，直到确认为前导码
    while qpsk_demodulate(signal[idx:], symbol_duration, sampling_rate, signal_freq, 24) != ref_bits:
        corr[idx] = 0
        idx = np.argmax(corr)
        if max(corr) < 1:
            return "未检测到前导码"
        print("Refine search, corr =", max(corr))
        # 如果为0的超过20次，说明前导码不对
        if np.sum(corr == 0) > 50:
            return "未检测到前导码"
    samples_per_symbol = int(sampling_rate * symbol_duration)
    signal_corrected = signal
    header_bit_len = 8 * 7  # 56 位
    needed_header_samples = (header_bit_len // 2) * \
        samples_per_symbol  # QPSK: 2 bit/符号
    if idx + needed_header_samples > len(signal_corrected):
        return "头数据长度不足"

    binary_header = qpsk_demodulate(
        signal_corrected[idx:],
        symbol_duration,
        sampling_rate,
        signal_freq,
        header_bit_len
    )
    print("binary_header: " + binary_header)
    header = Package(binary_header)
    data_length = header.get_length()
    print("data_length: " + str(data_length))

    # ========== 7. 解调包体数据 ==========
    # 同理，下方只是示例，需要多少位看协议定义
    package_bit_len = 8 * (data_length + 7)  # 这里预留7做示例
    needed_package_samples = (package_bit_len // 2) * samples_per_symbol
    if idx + needed_package_samples > len(signal_corrected):
        return "包数据长度不足"

    binary_package = qpsk_demodulate(
        signal_corrected[idx:],
        symbol_duration,
        sampling_rate,
        signal_freq,
        package_bit_len
    )
    package = Package(binary_package)

    # ========== 8. 提取并解码包数据 ==========
    package.data = bytes(int(package.data[i: i + 8], 2)
                         for i in range(0, len(package.data), 8))
    package.data = package.data.decode('ISO-8859-1')
    print("data: " + package.data)
    return package.data[:-1]


class Handler:
    def __init__(self):
        self.signal = np.array([])

    def add_signal(self, signal):
        signal = np.array(signal, dtype=np.float64) / 32767.0
        self.signal = np.concatenate((self.signal, signal))

    def handle_receive(self, symbol_duration, sampling_rate, signal_freq):

        # ========= 1. 带通滤波 =========
        self.signal = bandpass_filter(self.signal, signal_freq - 1000,
                                signal_freq + 1000, sampling_rate)
        self.signal = self.signal
        interval_samples = int(0.3 * sampling_rate)  # 0.2秒对应的采样点数
        
        window_size = int(0.25 * sampling_rate)  # 15ms窗口 

        noisy_window = self.signal[sampling_rate : window_size + sampling_rate]
        energy_threshold = np.sum(np.square(noisy_window)) * 1e6
        # energy_threshold = 1
        print("energy_threshold: ", energy_threshold)

        package_signal = []
        st = 0
        in_packet = False

        for i in range(0, len(self.signal) - window_size, window_size):
            window = self.signal[i:i + window_size]
            energy = np.sum(np.square(window))

            if energy < energy_threshold:
                if in_packet:
                    # 检查是否为包间隔
                    if i - st >= interval_samples:
                        package_signal.append(self.signal[st:i + window_size // 2])
                        in_packet = False
                else:
                    # 继续在间隔中
                    continue
            else:
                if not in_packet:
                    # 开始一个新的数据包
                    st = i
                    in_packet = True

        # 处理最后一个包
        if in_packet:
            package_signal.append(self.signal[st:])

        for i in range(len(package_signal)):
            package_signal[i] = handle_receive(package_signal[i], symbol_duration, sampling_rate, signal_freq)
            print(package_signal[i])
        print(len(package_signal))

        return "".join(package_signal)

    def clear_signal(self):
        self.signal = np.array([])


handler = Handler()
