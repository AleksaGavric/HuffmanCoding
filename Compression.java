import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class HuffmanNode {
   char ch;
   int frequency;
   HuffmanNode left;
   HuffmanNode right;

   HuffmanNode(char ch, int frequency,  HuffmanNode left,  HuffmanNode right) {
      this.ch = ch;
      this.frequency = frequency;
      this.left = left;
      this.right = right;
   }
}

class HuffManComparator implements Comparator<HuffmanNode> {
   
   @Override
   public int compare(HuffmanNode node1, HuffmanNode node2) {
      return node1.frequency - node2.frequency;
   }
}

public class Compression implements Huffman {

   private static Map<Character, Integer> getCharFrequency(String input) {
      HashMap<Character, Integer> freqMap = new HashMap();
      char[] charArray = input.toCharArray();

      for (char x: charArray) {
         if (freqMap.containsKey(x)) {
            freqMap.put(x, freqMap.get(x) + 1);
         } else {
            freqMap.put(x, 1);
         }
      }

      try {
         FileWriter writeToFile = new FileWriter(new File("freq.txt"));

         for (Map.Entry entry : freqMap.entrySet()) {
            String bitValue = String.format("%8s", Integer.toBinaryString((Character) entry.getKey())).replace(" ", "0");
            writeToFile.write( bitValue + ":" + entry.getValue() + "\n");
         }

         writeToFile.close();
      } catch (IOException e) {
         System.out.println("Error!");
         e.printStackTrace();
      }

      return freqMap;
   }

   private static HuffmanNode buildTree(Map<Character, Integer> map) {
      Queue<HuffmanNode> nodeQ = createQueue(map);

      while (nodeQ.size() > 1) {
         HuffmanNode node1 = nodeQ.remove();
         HuffmanNode node2 = nodeQ.remove();

         HuffmanNode node = new HuffmanNode('\0', node1.frequency + node2.frequency, node1, node2);
         nodeQ.add(node);
      }

      HuffmanNode root = nodeQ.remove();

      return root;
   }

   private static Queue<HuffmanNode> createQueue(Map<Character, Integer> map) {
      Queue<HuffmanNode> priorQ = new PriorityQueue<>(11, new HuffManComparator());

      for (Map.Entry<Character, Integer> entry : map.entrySet()) {
         priorQ.add(new HuffmanNode(entry.getKey(), entry.getValue(), null, null));
      }

      return priorQ;
   }

   private static Map<Character, String> createHuffman(HuffmanNode node) {
      Map<Character, String> map = new HashMap<>();
      createCode(node, map, "");

      return map;
   }

   private static void createCode(HuffmanNode node, Map<Character, String> map, String s) {
      if (node.right == null && node.left == null) {
         map.put(node.ch, s);

         return;
      }

      createCode(node.left, map, s + '0');
      createCode(node.right, map, s + '1');
   }

   private static String codeMessage(Map<Character, String> charCode, String input) {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < input.length(); ++i) {
         sb.append(charCode.get(input.charAt(i)));
      }

      return sb.toString();
   }

   public void encode(String inputFile, String outputFile, String freqFile) {
      BinaryIn getInput = new BinaryIn(inputFile);
      String input = getInput.readString();

      Map<Character, Integer> charFreq = getCharFrequency(input);
      HuffmanNode root = buildTree(charFreq);

      Map<Character, String> charCode = createHuffman(root);

      String encodedMessage = codeMessage(charCode, input);

      BinaryOut writeToFile = new BinaryOut(outputFile);

      for (int i = 0; i < encodedMessage.length(); ++i) {
         if (encodedMessage.charAt(i) == '0') {
            writeToFile.write("0");
         } else if (encodedMessage.charAt(i) == '1') {
            writeToFile.write("1");
         }
      }

      writeToFile.flush();
   }

   public void decode(String inputFile, String outputFile, String freqFile) {
      File myFile = new File(freqFile);
      Map<Character, Integer> charFreq = new HashMap<>();
      Scanner scn = null;

      try {
         scn = new Scanner(myFile);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }

      while (scn.hasNext()) {
         String line = scn.nextLine();

         String binary = line.substring(0,8);
         String frequencyy = line.substring(9);

         charFreq.put((char) Integer.parseInt(binary, 2), Integer.parseInt(frequencyy));
      }

      HuffmanNode root = buildTree(charFreq);
      String S = "";
      BinaryIn readData = new BinaryIn(inputFile);
      S += readData.readString();
      HuffmanNode end = root;
      StringBuilder output = new StringBuilder();

      while (!S.isEmpty()) {
         if (S.charAt(0) == '1') {
            end = end.right;
            S = S.substring(1);
         } else {
            end = end.left;
            S = S.substring(1);
         }
         if (end.left == null && end.right == null) {
            output.append(end.ch);
            end = root;
         }
      }

      BinaryOut writeOutput = new BinaryOut(outputFile);
      writeOutput.write(output.toString());

      writeOutput.flush();
   }

   public static void main(String[] args) {
      Huffman huffman = new Compression();

      huffman.encode("ur.jpg", "ur.enc", "freq.txt");
      huffman.decode("ur.enc", "ur_dec.jpg", "freq.txt");
   }
}
