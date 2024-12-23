package com.example.application.services;

import com.example.application.data.Company;
import com.example.application.data.Contact;
import com.example.application.data.Status;
import com.example.application.data.CompanyRepository;
import com.example.application.data.ContactRepository;
import com.example.application.data.StatusRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class CrmService {

    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    private final StatusRepository statusRepository;

    public CrmService(ContactRepository contactRepository,
            CompanyRepository companyRepository,
            StatusRepository statusRepository) {
        this.contactRepository = contactRepository;
        this.companyRepository = companyRepository;
        this.statusRepository = statusRepository;
    }

    public List<Contact> findAllContacts(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return contactRepository.findAll();
        } else {
            return contactRepository.search(stringFilter);
        }
    }

    public long countContacts() {
        return contactRepository.count();
    }

    public void deleteContact(Contact contact) {
        contactRepository.delete(contact);
        updateDataSql();
    }

    public void saveContact(Contact contact) {
        if (contact == null) {
            System.err.println("Contact is null. Are you sure you have connected your form to the application?");
            return;
        }
        contactRepository.save(contact);
        updateDataSql();
    }

    public List<Company> findAllCompanies() {
        return companyRepository.findAll();
    }

    public List<Status> findAllStatuses() {
        return statusRepository.findAll();
    }

    public Company addCompany(String name) {
        Company company = new Company();
        company.setName(name);
        return companyRepository.save(company);
    }

    private void updateDataSql() {
        List<Status> statuses = statusRepository.findAll();
        List<Company> companies = companyRepository.findAll();
        List<Contact> contacts = contactRepository.findAll();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/data.sql"))) {
            // Write statuses
            writer.write("INSERT INTO \"STATUS\" (ID, VERSION, NAME) VALUES");
            writer.newLine();
            for (int i = 0; i < statuses.size(); i++) {
                Status status = statuses.get(i);
                String sql = String.format("(%d, %d, '%s')%s",
                        status.getId(), status.getVersion(), status.getName(),
                        i < statuses.size() - 1 ? "," : ";");
                writer.write(sql);
                writer.newLine();
            }

            // Write companies
            writer.write("INSERT INTO \"COMPANY\" (ID, VERSION, NAME) VALUES");
            writer.newLine();
            for (int i = 0; i < companies.size(); i++) {
                Company company = companies.get(i);
                // Escape single quotes in company names
                String companyName = company.getName().replace("'", "''");
                String sql = String.format("(%d, %d, '%s')%s",
                        company.getId(), company.getVersion(), companyName,
                        i < companies.size() - 1 ? "," : ";");
                writer.write(sql);
                writer.newLine();
            }

            // Write contacts
            writer.write(
                    "INSERT INTO \"CONTACT\" (ID, VERSION, EMAIL, FIRST_NAME, LAST_NAME, COMPANY_ID, STATUS_ID) VALUES");
            writer.newLine();
            for (int i = 0; i < contacts.size(); i++) {
                Contact contact = contacts.get(i);
                String sql = String.format("(%d, %d, '%s', '%s', '%s', %d, %d)%s",
                        contact.getId(), contact.getVersion(), contact.getEmail(), contact.getFirstName(),
                        contact.getLastName(), contact.getCompany().getId(), contact.getStatus().getId(),
                        i < contacts.size() - 1 ? "," : ";");
                writer.write(sql);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
