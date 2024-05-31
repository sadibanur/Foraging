package learn.foraging.data;

import learn.foraging.models.Forager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ForagerFileRepository implements ForagerRepository {

    private final String filePath;

    private static final String DELIMITER = ",";
    private static final String DELIMITER_REPLACEMENT = "@@@";

    public ForagerFileRepository(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Forager> findAll() {
        ArrayList<Forager> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            reader.readLine(); // read header

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {

                String[] fields = line.split(",", -1);
                if (fields.length == 4) {
                    result.add(deserialize(fields));
                }
            }
        } catch (IOException ex) {
            // don't throw on read
        }
        return result;
    }

    @Override
    public Forager findById(String id) {
        return findAll().stream()
                .filter(i -> i.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Forager> findByState(String stateAbbr) {
        return findAll().stream()
                .filter(i -> i.getState().equalsIgnoreCase(stateAbbr))
                .collect(Collectors.toList());
    }

    @Override
    public Forager add(Forager forager) throws DataException, FileNotFoundException {
        List<Forager> all = findAll();
        all.add(forager);

        writeToFile(all);
        return forager;
    }


    private Forager deserialize(String[] fields) {
        Forager result = new Forager();
        result.setId(fields[0]);
        result.setFirstName(fields[1]);
        result.setLastName(fields[2]);
        result.setState(fields[3]);
        return result;
    }

    private String serialize(Forager forager) {
        StringBuilder buffer = new StringBuilder(200);

        buffer.append(forager.getId()).append(DELIMITER);
        buffer.append(forager.getFirstName()).append(DELIMITER);
        buffer.append(forager.getLastName()).append(DELIMITER);
        buffer.append(forager.getState());

        return buffer.toString();
    }

    private void writeToFile(List<Forager> all) throws FileNotFoundException, DataException {
        try (PrintWriter writer = new PrintWriter(filePath)) {
            writer.println("id,first_name,last_name,state");

            all.stream()
                    .map(this::serialize)
                    .forEach(writer::println);
        } catch (IOException ex) {
            throw new DataException("Could not write filepath: " + filePath);
        }
    }

}
