package com.example.multithreading_final.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlContactParser {

    // Регулярки для поиска по тексту
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\+?\\d[\\d\\s\\-()]{7,}\\d");

    private static final Pattern ADDRESS_PATTERN =
            Pattern.compile("(ул\\.|улица|пр\\.|проспект|пр-т|пл\\.|площадь|пер\\.|переулок|street|st\\.|road|rd\\.)" +
                            ".{5,80}",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    public static ParsedResult parse(String html, String baseUrl) {
        Document doc = Jsoup.parse(html, baseUrl);

        String text = doc.text();

        // 1. Пытаемся найти email/телефон в тексте
        String emailFromText = findFirst(EMAIL_PATTERN, text);
        String phoneFromText = findFirst(PHONE_PATTERN, text);
        String address = findFirst(ADDRESS_PATTERN, text);

        // 2. Пытаемся найти email/телефон в ссылках (mailto:, tel:)
        String emailFromLinks = null;
        String phoneFromLinks = null;

        Elements aTags = doc.select("a[href]");
        for (Element a : aTags) {
            String href = a.attr("href");
            if (href == null) continue;

            if (href.startsWith("mailto:") && emailFromLinks == null) {
                emailFromLinks = href.substring("mailto:".length()).trim();
            }
            if (href.startsWith("tel:") && phoneFromLinks == null) {
                phoneFromLinks = href.substring("tel:".length()).trim();
            }
        }

        String finalEmail = emailFromText != null ? emailFromText : emailFromLinks;
        String finalPhone = phoneFromText != null ? phoneFromText : phoneFromLinks;

        Set<String> links = extractLinks(doc);

        ParsedResult result = new ParsedResult();
        result.setEmail(finalEmail);
        result.setPhone(finalPhone);
        result.setAddress(address);
        result.setLinks(links);
        return result;
    }

    private static String findFirst(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        if (m.find()) {
            return m.group().trim();
        }
        return null;
    }

    private static Set<String> extractLinks(Document doc) {
        Set<String> links = new HashSet<>();
        Elements elements = doc.select("a[href]");
        for (Element a : elements) {
            String href = a.absUrl("href");
            if (href == null || href.isBlank()) continue;

            // mailto/tel нас не интересуют как "следующие страницы"
            if (href.startsWith("mailto:") || href.startsWith("tel:")) continue;
            if (href.startsWith("#")) continue;

            links.add(href);
        }
        return links;
    }

    public static class ParsedResult {
        private String email;
        private String phone;
        private String address;
        private Set<String> links;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Set<String> getLinks() {
            return links;
        }

        public void setLinks(Set<String> links) {
            this.links = links;
        }
    }
}
