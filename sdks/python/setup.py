"""
Setup configuration for Hypz Python SDK
"""

from setuptools import setup, find_packages

with open("README.md", "r", encoding="utf-8") as fh:
    long_description = fh.read()

setup(
    name="hypz-sdk",
    version="2.1.0",
    author="Hypz Team",
    author_email="support@hypz.io",
    description="Official Python SDK for Hypz Cloud Storage - Store, manage, and serve files with a simple API",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/ysr-hameed/hypz",
    project_urls={
        "Documentation": "https://hypz.io/docs",
        "Source": "https://github.com/ysr-hameed/hypz",
        "Tracker": "https://github.com/ysr-hameed/hypz/issues",
    },
    py_modules=["hypz"],
    classifiers=[
        "Development Status :: 5 - Production/Stable",
        "Intended Audience :: Developers",
        "Topic :: Software Development :: Libraries :: Python Modules",
        "License :: OSI Approved :: MIT License",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.7",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
        "Programming Language :: Python :: 3.10",
        "Programming Language :: Python :: 3.11",
        "Programming Language :: Python :: 3.12",
    ],
    python_requires=">=3.7",
    install_requires=[
        "requests>=2.25.0",
    ],
    extras_require={
        "dev": [
            "pytest>=6.0",
            "black>=21.0",
            "flake8>=3.9",
        ],
    },
    keywords="hypz cloud-storage file-storage s3 cdn upload storage files api sdk backblaze b2",
    license="MIT",
)
