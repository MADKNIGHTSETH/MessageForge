-- V3__Add_Recipient_To_Message.sql
ALTER TABLE messages ADD COLUMN recipient VARCHAR(255);
