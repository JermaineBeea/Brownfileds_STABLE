# Makefile for Robot Worlds

.PHONY: all compile test test-ref test-own version-release version-dev package release clean

MVN=mvn
POM=pom.xml
VERSION=$(shell grep -m1 '<version>' $(POM) | sed -E 's/.*<version>(.*)<\/version>.*/\1/')
RELEASE_VERSION=$(shell echo $(VERSION) | sed 's/-SNAPSHOT//')
DEV_VERSION=$(RELEASE_VERSION)-SNAPSHOT

all: compile

compile:
	$(MVN) clean compile

test: test-ref test-own

test-ref:
	@echo "Killing any existing reference server on port 5000..."
	- fuser -k 5000/tcp || true
	@echo "Running acceptance tests against reference server..."
	./scripts/start-reference-server.sh &
	REF_PID=$$!; \
	sleep 2; \
	$(MVN) test -Dtest=*Tests; \
	- kill $$REF_PID || true

test-own:
	@echo "Running acceptance tests against own server..."
	# Start own server in background (adjust as needed)
	./scripts/start-own-server.sh &
	OWN_PID=$$!; \
	sleep 2; \
	$(MVN) test -Dtest=*Tests; \
	kill $$OWN_PID

version-release:
	@echo "Setting version to $(RELEASE_VERSION) for release"
	sed -i 's/<version>$(VERSION)<\/version>/<version>$(RELEASE_VERSION)<\/version>/' $(POM)

version-dev:
	@echo "Setting version to $(DEV_VERSION) for development"
	sed -i 's/<version>$(RELEASE_VERSION)<\/version>/<version>$(DEV_VERSION)<\/version>/' $(POM)

package: compile
	$(MVN) package

release: version-release test package
	@echo "Tagging release as release-$(RELEASE_VERSION)"
	git add $(POM)
	git commit -m "Release version $(RELEASE_VERSION)"
	git tag release-$(RELEASE_VERSION)
	@echo "Release $(RELEASE_VERSION) built and tagged."

clean:
	$(MVN) clean
# Makefile for Robot Worlds
# Usage:
#   make compile         # Compile the code
#   make test-ref        # Run acceptance tests against reference server
#   make test-own        # Run acceptance tests against your own server
#   make version-release # Set version for release (removes -SNAPSHOT)
#   make version-dev     # Set version for development (adds -SNAPSHOT)
#   make package         # Package the software
#   make release         # Full release: version, test, package, git tag
#   make clean           # Clean build artifacts