DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

CREATE TABLE Filliale(
  FName VARCHAR(255),
  FStreet VARCHAR(255),
  FZip VARCHAR(10),
  PRIMARY KEY (FName, FStreet, FZip)

);


CREATE TABLE Product (
  ProductAsin VARCHAR(255) PRIMARY KEY,
  Title VARCHAR(255) NOT NULL,
  Salesrank INT NOT NULL,
  Bild VARCHAR(255)
);


CREATE TABLE ProductInFilliale (
  FName VARCHAR(255),
  FStreet VARCHAR(255),
  FZip VARCHAR(10),
  ProductAsin VARCHAR(255) REFERENCES Product (ProductAsin),
  IState VARCHAR(255) NOT NULL,
  Price VARCHAR(255),
  Avail BOOLEAN NOT NULL,
  PRIMARY KEY (ProductAsin, FName, FStreet, FZip),
  FOREIGN KEY (FName, FStreet, FZip) REFERENCES Filliale (FName, FStreet, FZip)
);




CREATE TABLE Book (
  BookAsin VARCHAR(255) PRIMARY KEY,
  Releasedate DATE NOT NULL CHECK (Releasedate <= CURRENT_DATE),
  Pages INT NOT NULL,
  ISBN VARCHAR(255) NOT NULL,
  FOREIGN KEY (BookAsin) REFERENCES Product (ProductAsin) ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE DVD (
  DVDAsin VARCHAR(255) PRIMARY KEY,
  Format VARCHAR(255) NOT NULL,
  Runtime INT NOT NULL,
  RegionCode VARCHAR(255) NOT NULL,
  FOREIGN KEY (DVDAsin) REFERENCES Product (ProductAsin) ON DELETE CASCADE ON UPDATE CASCADE	

);


CREATE TABLE Music (
  MusicAsin VARCHAR(255) PRIMARY KEY,	
  Releasedate DATE NOT NULL CHECK (Releasedate <= CURRENT_DATE),
  FOREIGN KEY (MusicAsin) REFERENCES Product (ProductAsin) ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE ArtistToMusic (
  MusicAsin VARCHAR(255) REFERENCES Music (MusicAsin),
  artistName VARCHAR(255),
  PRIMARY KEY (MusicAsin, artistName)
);


CREATE TABLE CreatorToMusic (
  MusicAsin VARCHAR(255) REFERENCES Music (MusicAsin),
  creatorName VARCHAR(255),
  PRIMARY KEY (MusicAsin, creatorName)
);


CREATE TABLE LabelToMusic (
  MusicAsin VARCHAR(255) REFERENCES Music (MusicAsin),
  labelName VARCHAR(255),
  PRIMARY KEY (MusicAsin, labelName)
);


CREATE TABLE TrackToMusic (
  MusicAsin VARCHAR(255) REFERENCES Music (MusicAsin),
  trackName VARCHAR(255),
  PRIMARY KEY (MusicAsin, trackName)
);


CREATE TABLE AuthorToBook (
  BookAsin VARCHAR(255) REFERENCES Book (BookAsin),
  authorName VARCHAR(255),
  PRIMARY KEY (BookAsin, authorName)
);


CREATE TABLE PublisherToBook (
  BookAsin VARCHAR(255) REFERENCES Book (BookAsin),
  publisherName VARCHAR(255),
  PRIMARY KEY (BookAsin, publisherName)
);


CREATE TABLE ActorToDVD (
  DVDAsin VARCHAR(255) REFERENCES DVD (DVDAsin),
  actorName VARCHAR(255),
  PRIMARY KEY (DVDAsin, actorName)
);


CREATE TABLE CreatorToDVD (
  DVDAsin VARCHAR(255) REFERENCES DVD (DVDAsin),
  creatorName VARCHAR(255),
  PRIMARY KEY (DVDAsin, creatorName)
);


CREATE TABLE DirectorToDVD (
  DVDAsin VARCHAR(255) REFERENCES DVD (DVDAsin),
  directorName VARCHAR(255),
  PRIMARY KEY (DVDAsin, directorName)
);


CREATE TABLE Category (
  Id SERIAL PRIMARY KEY,
  categoryName VARCHAR(255) NOT NULL,
  parentCategory INTEGER REFERENCES Category(Id)
);


CREATE TABLE ProductToCategory (
  ProductAsin VARCHAR(255) REFERENCES Product (ProductAsin),
  categoryId INTEGER REFERENCES Category (id),
  PRIMARY KEY (ProductAsin, categoryId)
);


CREATE TABLE SimilarProduct (
  Pnummer1 VARCHAR(255) REFERENCES Product (ProductAsin),
  Pnummer2 VARCHAR(255) REFERENCES Product (ProductAsin),
  PRIMARY KEY (Pnummer1, Pnummer2),
  CHECK (Pnummer1 <> Pnummer2)
);


CREATE TABLE Kunde (
  Username VARCHAR(255) PRIMARY KEY,
  Kontonummer VARCHAR(255),
  Lieferadresse VARCHAR(255)
);

CREATE TABLE Kauf (
  Username VARCHAR(255) REFERENCES Kunde (Username),
  FName VARCHAR(255),
  FStreet VARCHAR(255),
  FZip VARCHAR(255),
  ProductAsin VARCHAR(255) REFERENCES Product (ProductAsin),
  Datum TIMESTAMP NOT NULL CHECK (Datum <= CURRENT_TIMESTAMP),
  PRIMARY KEY (Username, Datum, ProductAsin),
  FOREIGN KEY (FName, FStreet, FZip) REFERENCES Filliale (FName, FStreet, FZip)
);



CREATE TABLE Feedback (
  Username VARCHAR(255) REFERENCES Kunde (Username),
  ProductAsin VARCHAR(255) REFERENCES Product (ProductAsin),
  Rating INT NOT NULL CHECK (Rating >= 1 AND Rating <= 5),
  fMessage TEXT NOT NULL,
  PRIMARY KEY (Username, ProductAsin)
);

CREATE TABLE GuestFeedback (
  Id SERIAL PRIMARY KEY,
  ProductAsin VARCHAR(255) REFERENCES Product (ProductAsin),
  Rating INT NOT NULL,
  fMessage TEXT NOT NULL
);

CREATE TABLE Error (
  Id SERIAL PRIMARY KEY,
  EntityName TEXT NOT NULL,
  ErrorMessage TEXT NOT NULL
);

--Calculating Rating
ALTER TABLE Product ADD COLUMN Rating DECIMAL(4,2);


CREATE OR REPLACE FUNCTION updateProductRating()
RETURNS TRIGGER AS $$
BEGIN
  UPDATE Product
  SET Rating = (
    SELECT AVG(rating)
    FROM (
      SELECT rating
      FROM Feedback
      WHERE ProductAsin = NEW.ProductAsin
      UNION ALL
      SELECT rating
      FROM GuestFeedback
      WHERE ProductAsin = NEW.ProductAsin
    ) AS subquery
  )
  WHERE ProductAsin = NEW.ProductAsin;

  RETURN NULL;
END;
$$ LANGUAGE plpgsql;



CREATE TRIGGER update_product_rating
AFTER INSERT OR UPDATE ON Feedback
FOR EACH ROW
EXECUTE FUNCTION updateProductRating();


CREATE TRIGGER update_product_rating_guest
AFTER INSERT OR UPDATE ON GuestFeedback
FOR EACH ROW
EXECUTE FUNCTION updateProductRating();

